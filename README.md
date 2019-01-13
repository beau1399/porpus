# Porpus - A Minimalist Leiningen Template for Web Development
* Server-Driven...
* But ClojureScript enabled for when you need it
* Suspicious of dependencies; HTTP is the framework...
* But "batteries" are nevertheless included
* Tight development loop
* __A quick path to making what you want to make__

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

5. Browse to http://localhost:3449/seshtest and refresh a few times. You will see an incrementing counter on the page built around your session.

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

The first two key/value pairs are inherent to how one responds to a valid request for a Web page; the status to return is the integer value 200, and what's being sent in response is "text/html".

After that, things get only marginally more complicated. There is the ":body" keyword, which tells Reitit that you're supplying the meat of the response you're wanting to serve up, followed by a call into [Hiccup](https://github.com/weavejester/hiccup) function "html5." It should thus be no surprise that Hiccup is a library that generates HTML5 strings from Clojure vectors. 

The syntax of Hiccup is intuitive, and closely matches the syntax used by Reagent on the client side. The most basic example looks something like this:

	[:span "Hello, world."]
	
So, the tag name goes into the leftmost position of the vector, as a Clojure keyword. The rightmost element in the vector is its inner HTML. Above this is just text, but nesting of HTML tags to any depth is, naturally, supported, e.g.:

	[:div [:span "Hello, world."]]
	
Between the tag name keyword and the inner HTML expression can come a hash map containing the element's attributes. In the "/buttontest" route declaration, the button tag that gets generated has an ":onclick" value, is a little string of JavaScript that serves as a good example of how to interfact handler.clj with the client-side code in ~/pptest/src/cljs/pptest/core.cljs:

    (ns pptest.core (:require [reitit.frontend :as reitit]))

    (defn ^:export greet [] (js/alert "Howdy!"))

You can add functions to this file and call them using similar syntax to what's seen in the button's ":onclick" handler. Not much other than the function name and the specific event being handled (":onchange" is another important one) will need to be changed. It is important to include the "^:export" metadata item on your functions; otherwise, name mangling in the production build will prevent the function from being found by the runtime script engine of the browser.

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

## Porpus Design in Depth

Porpus is designed to get your Clojure-based Web development efforts going as quickly and unobtrusively as possible. It grew out of my own needs, and out of a couple of competing factors I perceived in my own efforts to develop in the language:

__1. Clojure is powerful, expressive, and works with (not against) real, contemporary hardware.__

__2. Even for Lisp veterans, learning to apply Clojure is made difficult by the ecosystems most typical of Clojure Web development__

### The Power of Clojure

Expounding upon #1 above, one can start with the pretty tired old argument that homoiconicity, macros, and so on [make Lisp a better language than the ones that most people are using](http://www.paulgraham.com/avg.html). Truth and theory aside, 1) this argument has been slow to resonate with people actually writing code, and 2) there's a lot that can be added to it.

In fact, Robert C. Martin has gone so far as to claim that ["Clojure is the new C"](https://www.infoq.com/presentations/clojure-c). What does this mean? In his talk, Martin focused a lot on the disciplined, terse, symbol-heavy nature of the syntax of both languages, and there are plenty of people out there who'll tell you that he's "missing the point."

What rings true to me about the statement that "Clojure is the new C," though, is the glaring *need* for a new version of what C once was, and the potential that this might be filled by something like Clojure. The days when C was a useful model of how typical hardware actually operated are behind us- hence another provactively-titled link telling us that ["C is no longer a low level language](https://queue.acm.org/detail.cfm?id=3212479), subtitled "your computer is not a fast PDP-11."

Consider the computer you are probably reading this on. It probably has a CPU that uses things like branch prediction, register renaming engines, exquisitely complex caching mechanisms, and the like to present the carefully-crafted, somewhat leaky abstraction that it really is just a fast PDP-11. Running alongside this CPU is a GPU that much more closely resembles what computer engineers would have computer scientists working on nowadays if they had their druthers. 

Over time this compromised engineering will only grow leakier in its abstractions, more prone to nasty surprises, and generally less sustainable. There is a need for the engineering of software to better harmonize with the engineering of hardware. Most obvious to me, __languages need to discourage programmers from coding at a fast PDP-11, and make more modern paradigms easier to implement.__

In Clojure, we see this philosophy at work in the extra finger-tapping that is necessary to achieve something resembling assignment into a variable. That extra code also shunts what actually results into something within the constraints of functional programming. 

These are good things. I am no Kernighan, Ritchie, or Hickey, but [even I, when I did try my hand at language creation](https://beauscode.blogspot.com/2013/02/language-and-development-tool-for.html#reenter) realized that sequential code ought to look and feel fundamentally different from parallel code- as it does in Clojure. 

That is, I suspect, our future. Even if Clojure doesn't match people's preconceptions of a "low-level language," I am convinced that it's a less awkward abstraction of today's hardware than many languages that do match those preconceptions.

### The Banality of Dumpster Diving

So that's my take, at least, on why Clojure is what software developers need right now. 

Why aren't people using it? The people who get it -> nerd factor





what if learning C meant also learning... CS education

Tight dev loop
Batteries included
Figwheel.org - trying to learn too much
