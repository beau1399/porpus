# Porpus - A Minimalist Leiningen Template for Web Development
* Suspicious of dependencies; HTTP is the framework...
* But "batteries" _are_ included
* Server-Driven...
* But ClojureScript enabled for when you need it
* Tight development loop
* __A quick path to making what you want to make__

This document begins by describing how to generate a Porpus site, and then explains how to do the most basic Web development tasks for such a site. It is intended to serve as a self-contained tutorial that will have you doing Web development in Clojure very quickly, and it assumes only a basic understanding of how Web applications in general work and knowledge of basic Clojure data structures and function call syntax.

## Quick Start

This section will give you a quick path to a Porpus Web site, and show you what Porpus is and isn't. If you prefer to read about the design and intent of Porpus in greater depth, skip to the third major section of this document ("Porpus Design in Depth") and read that first. Otherwise, go ahead and follow these steps:

1. Get the Porpus repository onto your system
	cd ~
	mkdir porpus
	cd porpus
	git init
	git pull https://github.com/beau1399/porpus.git

2. Add Porpus to your Leiningen setup
	lein install

3. Make a Porpus projct
	cd ~
	lein new porpus-proj pptest

4. Run your project in debugging mode
	cd pptest
	lein figwheel

5. Out-of-the-box, a Porpus site can respond to a few URLs designed to be examples for you. Browse to http://localhost:3449/seshtest and refresh a few times. You will see an incrementing counter on the page built around your session.

6. Browse to http://localhost:3449/seshtest2. You will see the same counter value as on the last page, plus one.

7. Browse to http://localhost:3449/parmtest?n=99, replacing "99" with different integer values. You will see each integer echoed back to you in the page body.

8. Browse to http://localhost:3449/parmtest?n=string. You will get an error page, since the "n" parameter is expected to be an integer.

9. Browse to http://localhost:3449/buttontest and click the "Say hi" button. You will see an alert dialog.

From this little demo, a few pieces of information about Porpus can be gleaned. First, note that __most of the action takes place on the server side.__ Things like React, the single-page architecture, etc. are not a part of Porpus, at least not out of the box.

Second, realize that this server-centric design does not prevent scripting from happening, as evidenced by the (__ClojureScript-driven__) alert dialog seen in step 9. 

Finally, a couple of key Web capabilites are evident in this basic demo: __typed querystring parameters__ and __session persistence__.

## Dissecting the Demo

### Page "buttontest"

If you worked through the demo in the last section, you have a file on your computer at ~/pptest/src/clj/pptest/handler.clj. This file (or some Clojure file refactored out of it) is where I anticipate most of your development will take place, so taking it apart is a good next step in explaining Porpus.

Skipping past some boilerplate stuff that won't change much, we find the following around line 25:

     ["/buttontest"
      {:get {
       :handler (fn [stuff]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (html5 (head)
	   [:button {:onclick "pptest.core.greet()"} "Say hi"]  (include-js "/js/app.js"))})}}]
	   
What we see here is the Reitit route definition for page http://localhost:3449/buttontest. Such routes consist of a vector containing a route string ("/buttontest") and then a hash map that establishes how the route should be handled. 

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

## Porpus Design in Depth

Porpus is designed to get your Clojure-based Web development efforts going as quickly and unobtrusively as possible. It grew out of my own needs, and out of a couple of competing factors I perceived in my own efforts to develop in the language:

__1. Clojure is powerful, expressive, and works with (not against) real, contemporary hardware.__

__2. Even for Lisp veterans, learning to apply Clojure is made difficult by the ecosystems most typical of Clojure Web development.__

### The Power of Clojure

Expanding on #1 above, one can start with the pretty tired old argument that homoiconicity, macros, and so on [make Lisp a better language than the ones that most people are using](http://www.paulgraham.com/avg.html). Truth and theory aside, 1) this argument has been slow to resonate with people actually writing code, and 2) there's a lot that can be added to it.

In fact, Robert C. Martin has gone so far as to claim that ["Clojure is the new C"](https://www.infoq.com/presentations/clojure-c). What does this mean? In his talk, Martin focused a lot on the disciplined, terse, symbol-heavy nature of the syntax of both languages, and there are plenty of people out there who'll tell you that he's "missing the point."

What rings true to me about the statement that "Clojure is the new C," though, is the glaring *need* for a new version of what C once was, and the potential that this might be filled by something like Clojure. The days when C was a useful model of how typical hardware actually operated are behind us- hence another provactively-titled link telling us that ["C is no longer a low level language](https://queue.acm.org/detail.cfm?id=3212479), subtitled "your computer is not a fast PDP-11."

Consider the computer you are probably reading this on. It probably has a CPU that uses things like branch prediction, register renaming engines, exquisitely complex caching mechanisms, and the like to present the carefully-crafted, somewhat leaky abstraction that it really is just a fast PDP-11. Running alongside this CPU is a GPU that much more closely resembles what computer engineers would have computer scientists working on nowadays if they had their druthers. 

Over time this compromised engineering will only grow leakier in its abstractions, more prone to nasty surprises, and generally less sustainable. There is a need for the engineering of software to better harmonize with the engineering of hardware. Most obvious to me, __languages need to discourage programmers from coding at a fast PDP-11, and make more modern paradigms easier to implement.__

In Clojure, we see this philosophy at work in the extra finger-tapping that is necessary to achieve something resembling assignment into a variable. That extra code also shunts what actually results into something within the constraints of functional programming. 

These are good things. I am no Kernighan, Ritchie, or Hickey, but [even I, when I did try my hand at language creation](https://beauscode.blogspot.com/2013/02/language-and-development-tool-for.html#reenter) realized that sequential code ought to look and feel fundamentally different from parallel code- as it does in Clojure. 

That is, I suspect, our future. Even if Clojure doesn't match people's preconceptions of a "low-level language," I am convinced that it's a less awkward abstraction of today's hardware than many languages that do match those preconceptions.

### The Banality of Dumpster Diving

If we allow that the argument made in the last section is plausible, and that Clojure is a valid, even inevitable direction for Web development, then an obvious question presents itself: why are people using other things instead? 

If choosing Clojure were simply a matter of learing its syntax, and learning how to think in terms of functions and immutable data instead of variables and objects, then I think the transition to Clojure would be faster and easier than it has been. This is not the case, though. The [Figwheel documentation](https://figwheel.org/docs/getting_help.html) says some things about this that ring true to me:

*"First, folks try to learn too many things in parallel. They try to learn functional programming, persistent datastructures, ClojureScript tooling, hot reloading, using a browser connected REPL, Reactjs, a ClojureScript React wrapper like Reagent, Javascript, Ring(ie. Rack for Clojure), setting up a Clojure webserver all at the same time.*

*This layering strategy may be an efficient way to learn when one is learning an imperative programming language like Python, Ruby or JavaScript. It becomes a losing strategy when you start to work with ClojureScript. The biggest reason for this is that the language itself is significantly different than these imperative languages. There are enough differences that you will find it difficult to associate these new patterns with the programming patterns that you are accustomed to. This unfamiliarity is easily compounded when you then add several other paradigm breakers like Reactjs and hot reloading to the mix."*

To this depressing narrative I would add that the Clojure novice will need to deal with Emacs, Marmalade, Cider, and Leiningen to even get to the point where the things mentioned above are even an issue. There are myriad little pitfalls involved with even getting these things up-and-running, to say nothing of learning to use them. I had to deal with the fact that my distribution's package manager offered only version 24 of Emacs, for example, which simply does not support Marmalade. 

There are ways around such problems, but the associated time costs add up quickly and are offset against time actually spent learning Clojure itself. 

The Figwheel docs offers a survival strategy which is probably not ideal for the more practically-minded among us:

*"The solution is to keep things as simple as possible when you start out. Choose finite challenges like learning enough of the ClojureScript language to where you can express complex things before you attempt to manipulate a web page. From there attempt to simply manipuate the DOM with the goog.dom API. Once you have a handle on that start exploring React and how to use sablono to create a dynamic web site. Then start exploring Clojure and create a simple webserver with Ring."*

Paraphrasing, this advice tells us to learn a fair amount of syntax without attempting a practical application of it. Then, throw these four libraries into the mix and you'll get to make a "simple webserver." Even more depressing is the segue into this: 

*"As developers, we become very accustomed to having tools set up just the way we like them. I’m very sympathetic to this.*

*I however strongly advise that you not invest too much time trying to set up a sweet development environment, and rather invest that time into learning the ClojureScript language with a simple set of tools.*

*The Clojure landscape currently has a diverse set of tools that is constantly in flux... If you spend a lot of time evaluating all these options it can become very frustrating. If you wait a while, and use simple tools you will have much more fun actually using the language itself and this experience will provide a foundation to make better tooling choices.*

*If you are new Clojure and ClojureScript I’d advise that you start with a terminal REPL and a decent editor."*

In other words, prepare yourself to work brain-teasers in something resembling a VT-100 session for a good while before you even try and decide what your real toolset will look like. __This is all very solid advice in its own way, but reading it, is there any wonder so many people throw up their hands and just decide to keep writing dubious Java?__

Reading through this pastiche of 




Why aren't people using it? The people who get it -> nerd factor





what if learning C meant also learning... CS education

Tight dev loop
Batteries included
Figwheel.org - trying to learn too much

TODO - FORM POST EXAMPLE IN THE INTEREST OF BATTERIES INCLUDED
TODO - request and session params to handler fucntions

there are drawbacks to things like react anyway; people cite scalability of foisting shit on the client; but scalabilit can be defined as the ability to address perf. probs by adding hardware. sounds more like something to be done on server side, to say nothing of varying JS implementations over which we have no control. and is the UX better? (sliding panels)

future directions / e.g.s
 anti-forgery
 auth
 db
