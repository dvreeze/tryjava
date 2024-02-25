Trying out Java again
=====================

Trying out Java (21), JDK APIs, functional programming, immutable collections etc.

Moving from Scala back to Java
------------------------------

### What I like about Scala

Going back from Scala to Java is a bit painful for me, in that I really like Scala as a language. Fortunately modern
versions of Java (such as Java 21) offer a far better developer experience than older versions. For example, with
*type inference for local variables* (using the "var" keyword) and *record classes* the amount of boilerplate code decreases
significantly.

What is it that I really like and benefited from in Scala? For example:

+ Scala offers thread-safe immutable collections (through its standard library)
+ Scala is a great language for domain modeling
+ Scala supports functional effect systems (ZIO 2, etc.)
+ Scala supports "FP light" (and much more) in general (writing *deterministic total pure functions* is natural in Scala)
+ Scala offers term derivation by the compiler ("implicits"), in particular the improved "implicits" in Scala 3

This helped me a lot in the past. For example, *thread-safe immutable collections* are great when developing an XML
library (which I did with [yaidom](https://github.com/dvreeze/yaidom)), supporting in-memory collections of read-only
XML "DOM" trees safely shared by multiple threads (without suffering from memory visibility problems).

I used Scala's domain modeling strengths when developing [XBRL](https://www.sbr-nl.nl/over-sbr/wat-is-sbr/xbrl)
support (on top of above-mentioned XML library). This combination of *bottom-up domain modeling* and
*top-down application development* on top of that domain enables very quick and robust (XBRL) application development
once the coded domain is there.

I like *functional effect systems* like [ZIO 2](https://zio.dev/) a lot, but I don't know about many projects using it.
Even Scala projects in general are a bit scarce, compared to Java projects. But "FP light", or the habit of moving
towards the usage of *deterministic total pure functions*, certainly helps create programs that work well, and that
support *local reasoning* about code. This habit goes well together with using *deeply immutable data structures* and
immutable collections in particular. Of course, in practice this must be balanced against programming habits imposed
on us by used frameworks and libraries.

Scala's *implicits* (when used in moderation) are a compile-time safe alternative to many applications of Java
reflection.

How would Java (at least in theory) stack up nowadays against my points above about what I like about Scala? Not too
bad:

+ [Guava](https://guava.dev/) (or alternative libraries) offer (widely used) thread-safe immutable collections to Java
+ With record classes, sealed interfaces, etc., Java has evolved into a powerful domain modeling language
+ I don't yet know about functional effect systems in Java
+ "FP light" is well supported now in Java, e.g. due to record types, immutable collections (e.g. Guava) and *Streams*
+ Java has no "implicits", so in Java we are stuck with reflection

So other than implicits versus reflection, Java does fine in the points that I like and benefited from in Scala.
In particular, the above-mentioned "yaidom" XML library could also be ported to Java 21 without too much effort.

### Different philosophies of Scala and Java

Of course, the philosophies behind Scala and Java differ. In spirit, Scala is not married to the JVM. It tries to marry
(modern) *OO with FP* in a *statically typed language*, offering a sound *orthogonal* set of language features, backed by an
*advanced type system* with a *strong theoretical foundation*. You could say that Scala equally values *nouns* and *verbs*
(instead of being Steve Yegge's
[kingdom of nouns](http://steve-yegge.blogspot.com/2006/03/execution-in-kingdom-of-nouns.html)).
That brilliant article was written in 2006, though. Starting with Java version 8,
[lambdas and functional interfaces](https://www.baeldung.com/java-8-lambda-expressions-tips) have changed this for
the better. By the way, note that Scala's perception of OO is not an endorsement of mutability-by-default.
Modern OO is not about mutability.

Still, compared to Scala, Java is *more closely bound to the JVM*, and therefore less orthogonal. Where do we see that?
E.g.:

+ We extend traits (and classes) in Scala, but in Java we *extend* classes yet *implement* interfaces
+ In Java we have *static* members, that we cannot abstract about, whereas in Scala singleton objects can extend traits
+ In Java *primitives* and (heap) *objects* are clearly distinct, whereas in Scala this distinction is less prominent
+ In Java *arrays* and *collections* differ in selection syntax; Scala sees arrays/collections as "mathematical functions"
+ In Java *fields* and *methods* are different; Scala treats "val" as a special case of "def" ("ignoring/hiding" fields)
+ In Java *methods* and *operators* are distinct concepts, but in Scala "operators" are just methods with different names

Also, using *null* (null reference) in Scala is considered a bad practice. Scala has its *Option* type, like Java has its
*Optional* type, but "nullability" in Java is part of the development experience.

### Ideas about programming in general

In general, some things I find important in programming:

+ Ability to *reason locally about code*
+ A clear "mental model" of the *runtime behaviour/assumptions* of a framework/library (ZIO, Futures, servlets, etc.)
+ Disciplined use of packages/namespaces, with (almost) only *unidirectional dependencies*
+ On DRY versus *unwanted dependencies*: undesirable (often ad-hoc) dependencies hurt more than some code repetition
+ Reducing mental load, striving for *simplicity*

Regarding simplicity, a very important article is Li Haoyi's
[Principle of Least Power](https://www.lihaoyi.com/post/StrategicScalaStylePrincipleofLeastPower.html).

An example of different runtime behaviour and hidden assumptions is ThreadLocal in one-thread-per-request servlets
used in combination with Futures that use an underlying thread pool ("breaking" the ThreadLocals).

An attractive style of programming is the FP style of "monadic" chains of *higher-order function calls*, such as Scala
collection *transformations* or even Java's Stream API. In this programming style we hardly find loops, if-else statements,
and side-effects.  The result is easier to reason about, and more concise and to the point.

In summary, what I find important in programming is mostly related to *discipline*, to limiting myself to practices that
work well at a larger scale. But that's of course my perspective, coming from my experiences.

Of course, the above is about Scala and Java, and not about practices in "enterprise programming", programming cloud-based
software, etc., which are topics that are at least as interesting as the chosen programming language.

