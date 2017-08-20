# clj-web-repl

Ring middleware that provides a web-accesible repl into your web application.

### Security

You need to hide this behind very restrictive access (developers only!).

### Todo
- Proper error handling.
- Persistent repl sessions. 
- Repl recall across page loads using local history.


### Usage

```clojure
(run-jetty
  (wrap-repl-console
    (fn [request]
      (serve-page request))
    {:root "/admin"})
  {:port 3000})
  
  
; the console will be accessible at
; http://localhost:3000/admin/index.html
```

### Licensing

MIT


