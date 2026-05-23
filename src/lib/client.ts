import { hc } from "hono/client"
import { HTTPException } from "hono/http-exception"
import superjson from "superjson"

declare global {
  interface Window {
    Clerk?: {
      session?: {
        getToken: () => Promise<string | null>
      } | null
    }
  }
}

const getBaseUrl = () => {
  if (typeof window !== "undefined") {
    return ""
  }

  return process.env.NODE_ENV === "development"
    ? "http://localhost:3000/"
    : process.env.VERCEL_URL
      ? `https://${process.env.VERCEL_URL}`
      : "https://<YOUR_DEPLOYED_WORKER_URL>/"
}

const getClerkToken = async () => {
  if (typeof window === "undefined") {
    return null
  }

  return window.Clerk?.session?.getToken() ?? null
}

export const baseClient = hc<any>(getBaseUrl(), {
  fetch: async (input: RequestInfo | URL, init?: RequestInit) => {
    const token = await getClerkToken()
    const headers = new Headers(init?.headers)
    if (token && !headers.has("Authorization")) {
      headers.set("Authorization", `Bearer ${token}`)
    }

    const response = await fetch(input, {
      ...init,
      cache: "no-store",
      headers,
    })

    if (response.status >= 400) {
      const message = await response
        .clone()
        .json()
        .then((body) => body?.message ?? response.statusText)
        .catch(() => response.statusText)

      throw new HTTPException(response.status as any, {
        message,
        res: response,
      })
    }

    const contentType = response.headers.get("Content-Type")

    response.json = async () => {
      const text = await response.text()

      if (contentType === "application/superjson") {
        return superjson.parse(text)
      }

      try {
        return JSON.parse(text)
      } 
      catch (error) {
        console.error("Failed to parse response as JSON:", error)
        throw new Error("Invalid JSON response")
      }
    }
    return response
  },
})["api"]

function getHandler(obj: Object, ...keys: string[]) {
  let current = obj
  for (const key of keys) {
    current = current[key as keyof typeof current]
  }
  return current as Function
}

function serializeWithSuperJSON(data: any): any {
  if (typeof data !== "object" || data === null) {
    return data
  }
  return Object.fromEntries(
    Object.entries(data).map(([key, value]) => [
      key,
      superjson.stringify(value),
    ])
  )
}

function createProxy(target: any, path: string[] = []): any {
  return new Proxy(target, {
    get(target, prop, receiver) {
      if (typeof prop === "string") {
        const newPath = [...path, prop]

        if (prop === "$get") {
          return async (...args: any[]) => {
            const executor = getHandler(baseClient, ...newPath)
            const serializedQuery = serializeWithSuperJSON(args[0])
            return executor({ query: serializedQuery })
          }
        }

        if (prop === "$post") {
          return async (...args: any[]) => {
            const executor = getHandler(baseClient, ...newPath)
            const serializedJson = serializeWithSuperJSON(args[0])
            return executor({ json: serializedJson })
          }
        }

        return createProxy(target[prop], newPath)
      }
      return Reflect.get(target, prop, receiver)
    },
  })
}

export const client: any = createProxy(baseClient)
