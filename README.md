# Porpus - A Minimalist Leiningen Template for Web Development in Clojure and ClojureScript

Porpus lies somewhere between the client-heavy paradigm of Reagent and the server-heavy paradigm of Compojure. I couldn't find a template that hit this sweet spot for me, so I made one.

Porpus:
* Is suspicious of dependencies; HTTP is the framework...
* But "batteries" _are_ included
* Is server-driven...
* But ClojureScript-enabled for when you need it
* Has a tight development loop
* __Offers a quick path to making what you want to make__

This document begins by describing how to generate a Porpus site, which comes complete with examples of how to do basic Web development tasks for you to clone and extend. This is intended to serve as a self-contained tutorial that will have you doing Web development quickly. It assumes only a basic understanding of how Web applications in general work, along with knowledge of basic Clojure data structures and function call syntax.

## Quick Start

This section will give you a quick path to a Porpus Web site, and show you what Porpus is and isn't. If you prefer to read about the design and intent of Porpus in greater depth, skip to the third major section of this document ("Porpus Design in Depth") and read that first. Otherwise, go ahead and follow these steps:

#### Get the Porpus repository onto your system

     cd ~
     mkdir porpus
     cd porpus
     git init
     git pull https://github.com/beau1399/porpus.git

#### Add Porpus to your Leiningen setup

     lein install

#### Make a Porpus projct

     cd ~
     lein new porpus-proj pptest

#### Run your project in debugging mode

     cd pptest
     lein figwheel
     
## Using the demo site

Out-of-the-box, a Porpus site can respond to a few URLs designed to be examples for you. Browse to http://localhost:3449/seshtest and refresh a few times. You will see an incrementing counter on the page built around your session.

#### Sessions

Browse to http://localhost:3449/seshtest2. You will see the same counter value as on the last page, plus one.

#### Querystring Parameters

Browse to http://localhost:3449/parmtest?n=99, replacing "99" with different integer values. You will see each integer echoed back to you in the page body.

Browse to http://localhost:3449/parmtest?n=string. You will get an error page, since the "n" parameter is expected to be an integer.

#### Scripting

Browse to http://localhost:3449/buttontest and click the "Say hi" button. You will see an alert dialog.

#### Forms

Browse to http://localhost:3449:/formtest, enter some text into the text box, and press the "OK" button. The page will post back to the server and return another page that uses your input in a message.

#### Summary

From this little demo, a few pieces of information about Porpus can be gleaned. First, note that __most of the action takes place on the server side.__ Things like React, the single-page architecture, etc. are not a part of Porpus, at least not out of the box.

Second, realize that this server-centric design does not prevent scripting from happening, as evidenced by the (__ClojureScript-driven__) alert dialog seen in step 9. 

Finally, a couple of key Web capabilites are evident in this basic demo: __typed querystring parameters__ and __session persistence__.

## Dissecting the Demo

### Page "buttontest"

If you worked through the demo in the last section, you have a file on your computer at ~/pptest/src/clj/pptest/handler.clj. This file (or Clojure file(s) refactored out of it) is where I anticipate most of your development will take place, so taking it apart is a good next step in explaining Porpus.

Skipping past some boilerplate stuff that won't change much, we find the following around line 25:

     ["/buttontest"
      {:get {
       :handler (fn [stuff]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (html5 (head)
	   [:button {:onclick "pptest.core.greet()"} "Say hi"]  (include-js "/js/app.js"))})}}]
	   
What we see here is the [Reitit](https://github.com/metosin/reitit) route definition for page http://localhost:3449/buttontest. Such routes consist of a vector containing a route string ("/buttontest") and then a hash map that establishes how the route should be handled. 

The outer hash map for the route ties keyword ":get" (the HTTP verb supported here) to a nested map. In this simple example, the nested map need only define a value for keyword ":handler", which is tasked with generating and returning yet another map that gives all of the data about the HTTP response.

The first two key/value pairs of this latest map are inherent to how one responds to a valid request for a Web page; the status to return is the integer value 200, and what's being sent in response is "text/html".

After that, things get only marginally more complicated. The value attached to the ":body" keyword supplies the meat of the response you're wanting to serve up, which is all contained with a call into [Hiccup](https://github.com/weavejester/hiccup) function "html5." It should thus be no surprise that Hiccup is a library that generates HTML5 strings from Clojure vectors. 

The syntax of Hiccup is intuitive, and closely matches the syntax used by Reagent on the client side. The most basic example looks something like this:

	[:span "Hello, world."]
	
So, the tag name goes into the leftmost position of the vector, as a Clojure keyword. The rightmost element in the vector is its inner HTML. Above this is just text, but nesting of HTML tags to any depth is, naturally, supported, e.g.:

	[:div [:span "Hello, world."]]
	
Between the tag name keyword and the inner HTML expression can come a hash map containing the element's attributes. In the "/buttontest" route declaration, the button tag that gets generated has an ":onclick" value, is a little string of JavaScript that serves as a good example of how to interfact handler.clj with the client-side code in ~/pptest/src/cljs/pptest/core.cljs:

    (ns pptest.core (:require [reitit.frontend :as reitit]))

    (defn ^:export greet [] (js/alert "Howdy!"))

You can add functions to this file and call them using similar syntax to what's seen in the button's ":onclick" handler. Not much other than the function name and the specific event being handled (":onchange" is another important one) will need to be changed. It is important to include the "^:export" metadata item on your functions; otherwise, name mangling in the production build will prevent the function from being found by the runtime script engine of the browser.

At the end of the body definition is a call to function "include-js". This brings in "app.js", which at runtime will hold an amalgamation of "core.js" and all of the other things necessary to bridge the gap between your ClojureScript and the JavaScript the browser can actually execute. This is at the end of the body definition so that it can be relied on to load after the page markup has loaded.

Finally, the "(head)" function call deserves explanation. This call is repeated across all of the demo's routes, and it abstracts away some unexciting things like character set, viewport, and the inclusion of the proper (minimized vs. full) version of the site CSS file. This is done using the same Hiccup syntax that's used to generate the rest of the response body:

     (defn head []
       [:head
        [:meta {:charset "utf-8"}]
        [:meta {:name "viewport"
                :content "width=device-width, initial-scale=1"}]
        (include-css (if (env :dev) "/css/site.css" "/css/site.min.css")

### Page "parmtest"

The "/parmtest" route declaration is similar to that of "/buttontest", with some additional parameter-related features:

     ["/parmtest"
        {:get {
	       :coercion reitit.coercion.spec/coercion
               :parameters { :query  {:n int?} }
               :handler (fn [{ {n :n} :params }]
                    {:status 200
                     :headers {"Content-Type" "text/html"}
                     :body (html5 (head)
		     	   [:span (str "Parameter is " n)]
		        (include-js "/js/app.js"))})}}]
			
The "coercion" key/value pair is new. As a concept, "coercion" refers to the translation of querystring parameters, which are freeform text, into values meeting the more strongly typed needs of a Web application. The "coercion" key/value pair seen in the snippet above simply brings in the Reitit coercion library that's included in the Porpus development stack out-of-the-box, and can generally be repeated without any special attention for any route in need of parameter coercion.

The next key/value pair establishes what this particular route expects parameter-wise. First, we are relying on query-based parameters, as denoted by the ":query" keyword; ":body" would be another option here, if we were using a POST request. The ":query" keyword is tied to a nested map consisting of a name "n", followed by a validation function (Clojure's "int?" predicate). 

Beyond that, little new remains to explain other than how the handler function's parameter list must be constructed. It is a destructuring map that pulls the value attached to keyword ":n" in the ":params" map of the object passed to all requests. In other words, given the ":parameters" declaration seen above it, we can expect that the handler function will be passed a map that takes, in part, the form 
    
     {:params {:n "value of the parameter"}}
     
and the declaration above will place the value ("value of the parameter" above, an integer when the route is correctly used) into handler function parameter "n". This handler function parameter is inserted into a span in the response body, to achieve the effect seen in the demo.

### Pages "seshtest" and "seshtest2"

The route for "/seshtest" is found after the one for "/parmtest" and looks like this:

     ["/seshtest"
        {:get {:handler (fn [{session :session}]
                              {:status 200 :headers {"Content-Type" "text/html"}
                               :session (assoc session :markuse (inc (:markuse session 0)))
                               :body (html5 (head)[:body [:span (:markuse session 0)]])})}}]

The route declaration for "/seshtest2" is identical except for the initial string. It exists to demonstrate the cross-page nature of the session facility.

First, take note of the parameter list for the handler function. Here, destructuring is used to extract the value of key ":session" of the request object into a parameter called "session". This itself is a hash map, and the key used for the incrementing counter evident on "seshtest" and "seshtest2" is named "markuse".

The new value of the session after the request is responded to must be included in the map returned by the handler function:

     :session (assoc session :markuse (inc (:markuse session 0)))
     
In pseduocode, this line says "return a session map with :markuse set to 1 plus its value in the parameter session, or 1 plus 0 if :markuse is absent from the parameter session." Within the HTML body of the response, the value of :markuse is displayed, once again substituting 0 for the absent value:

     [:span (:markuse session 0)]

These session mechanics could hardly be simpler. Note, though, that there are a few things in handler.clj that exist to achieve this seamless developer experience. These shouldn't require much consideration, but they are necessary because of a key aspect of Reitit: the middleware stack is constructed on a per-route basis. So, by default, the session store associated with one Reitit route won't be the store associated with another. 

This is addressed by creating a single session store, using the following line of code:

     (def store (memory/memory-store))
     
Much farther down in handler.clj, this is integrated into the middleware of the Reitit route map.

### Page "formtest"

The definition of route "/formtest" is different from those seen previously in its use of both a ":get" key/value pair and a ":post" key/value pair:

    ["/formtest"
      {:get {:coercion reitit.coercion.spec/coercion             
             :handler (fn [requestobj]
                    {:status 200
                     :headers {"Content-Type" "text/html"}
                     :body (html5 (head)
			  [:form { :method "post" :action "/formtest"}
			   [:input {:name "username"}]
			   [:input {:type "submit" :value "OK"}]])})}
       :post {:parameters {:body {:username string?}}
              :handler (fn  [{ {u :username} :params session :session ip :remote-addr}]
               {:status 200
                :headers {"Content-Type" "text/html"}
                :body (html5 (head) [:span (str "Hi, " u " from " ip ".")])})}}]

The same ":coercion" value as seen in the "/parmtest" route is evident here. Though the parameter(s) for a POST will be passed in the request body, not its query string, the same Reitit library code will handle it. One thing to remember is that the ":coercion" pair goes on the handler for GET. This may seem somewhat counterintuitive, in that it is the POST that ultimately has expectations for the typing of the parameter, but this is just how Reitit coercion operates.

The GET handler is straightforward. Its handler accepts the single request object parameter; note that Reitit expects to pass this in, if nothing else, and omitting it will cause problems. The handler goes on to render a simple form that accepts a single piece of data and performs a POST back to the same route. 

The POST parameter is more complex in the parameters it expects to receive. As seen in "/parmtest", the ":params" member of the request object is destructured to get the desired parameter. I've named the parameter "u", but the relevant key in the ":params" map is ":username", which is derived from the "name" attribute of the form input. 

The ":session" member of the request object is also accepted, as seen in the "/seshtest" route. It is not used in the example code above, but is included in recognition of the fact that a user's session data will often be relevant to anything he or she attempts to post. One might not want to allow a user to POST a checkout request on a commerce site without an authenticated session, for example.

Note that session is not included in the response here. This is acceptable since it is not actually modfied.

A new key/value pair in the destructured parameter declaration is "ip." This _is_ parroted back in the HTML returned by the POST handler, and is mostly included just to show how one obtains the IP address within the Porpus ecosystem.

### Production Deployment

Creating a production-ready .JAR file that contains an embedded Jetty HTTP server is easy. Returning to the "pptest" example, the commands to create this file are as shown below:

     cd ~/pptest
     lein uberjar
     
The "lein" command will output the full paths to two .JAR files, one with "standalone" in the name and another without. The latter file can be passed to Java for execution thus:

     java -jar ~/pptest/target/pptest.jar
     
By default, traffic will be served on port 3000. This can be changed by editing file ~/pptest/src/clj/pptest/server.clj, where the port number "3000" is evident near the bottom of the file. 

However, note that the default HTTP port, 80, may be off-limits, especially when running Jave without "sudo" in front. Configuring your Web server to pass traffic through from port 80 to port 3000 (or another high-numbered port) may therefore be a better long-term option.

## Porpus Design in Depth

What you see here grew pretty quickly out of some specific needs I had, and out of the lack of a template that matched them. 

The __Reagent template__ includes many of the components used to build Porpus (Reitit, Ring, etc.) but also has some drawbacks. As far as I can tell, it lacks out-of-the-box features friendly to server-centric development, such as parameter coercion and cross-page session persistence. Also, if you don't want to use React, Reagent is more than you need.

The __Compojure template__ is more server-centric, but I am wary of the fact that the generated project doesn't include a single ClojureScript file. No doubt there is a way to incorporate ClojureScript into a Compojure project, but if I am going to have to do this for every such project, I decided, why not just make my own template? Also, I found the Compojure documentation a bit sparse, although that may just reflect the fact that Compojure is a routing library and not much else. 

#### Porpus and Reagent

One design decision I faced in making Porpus was whether or not I should include Reagent (a React wrapper) in my template. Ultimately, __I decided not to include Reagent__ in the default Porpus development stack. Though there are good arguments for including React in a new project, in each case I was able to come up with a good counterargument. 

First, I don't accept the argument that doing everything on the client is scalable. I think a good definition of "scalability" is the ability to address performance problems by adding hardware. The developer has zero control over client-side hardware. He must rely on the user devices being performant, their JavaScript engines being optimal and compatible, and so on. 

Second, I do not generally want a "rich" / lazy loaded user interface. In my experience, these UIs tend to either shift around in disjointed fashion as they load and render, or they have to include extra placeholders to prevent that. The jerkiness and/or placeholders can extend over an unacceptably long period of time. This is not a performance problem to be addressed by speeding things up, but a sub-par architecural choice- even if a server-rendered page takes 10 seconds to load up, at least I know when it's loaded and where to put my finger down to follow a link.

Finally, I get a full does of React in my full-time job. What I want to do with the work at hand is to write good Clojure. Toward that end, I have made an effort to minimize the number of novel things in play other than Clojure itself. I think many developers might benefit from that.

All that said, if you do want to make the sort of UI that React is so good at making, then I would suggest that Clojure / Reagent is the best development stack for doing that. So much of good React development consists of carefully striving for immutability. Even after you get into the habit of, say, returning new objects build using the "..." syntax in JavaScript, there are pitfalls that can introduce mutations in non-obvious ways. Clojure, on the other hand, builds immutability into the language. It is a natural fit for React development.

Also, there is nothing in Porpus that's incompatible with React or Reagent. I have developed projects that make use of the Reagent libraries for narrowly-targeted purposes, and it is trivial to introduce Reagent into a Porpus project.

## Licensing

In order to facilitate the most widespread usage possible, the Porpus template is licensed under the [Unlicense](http://unlicense.org/). Generated projects are also licensed with this license by default, since GitHub is of the opinion that software without any license at all remains the copyrighted property of its creator(s). However, you are free to apply whatever license you want to code generated using the Porpus template.
