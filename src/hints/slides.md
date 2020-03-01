<!-- .slide: class="title" -->

# Testrecorder
## Record - Refactor - Replay

<div class="speaker">Max Bechtold, Stefan Mandel</div>

---

# What is Legacy Code?

* Usually few (if any) automated tests
* Contains uncontrolled side effects
  * e.g. on arguments or global variables
* Is not really object-oriented
  * ignores patterns
  * uses generic types like maps instead of writing classes
  * classes used as structs (data without logic)
* No stringent exception handling
  * e.g. both runtime and checked, inconsistent logging, ...

----

# How can we work with it?

* Refactor?
  * but code is untested, prone to errors
* Write tests first?
  * but you don't understand what the code is (supposed to be) doing
  * if you do, the code is hardly testable
* Characterization tests to the rescue!
  * but it's a tiresome and uninteresting task
  * timeconsuming and error prone

----

# Enter: testrecorder

* Testrecorder takes on the repetitious part of characterization tests
  * **Record** for each function
    * its pre state
    * and post state
  * **Refactor**
    * your task as the developer
    * now with generated tests as a safety net
  * **Replay**
    * Establish the state prior to execution of each function
    * Compare its resulting state with that previously recorded 

---

# Setup

* Java 8 (JDK) is required, ensure that your IDE uses the JDK and not the bundled JRE
* Most IDEs support JUnit4 and JUnit5 but testrecorder generated tests are still JUnit4-only
* Some nasty IDEs do not allow editing and searching in generated/derived sources without further configuration

----

* The sources are available
  * as clone from https://github.com/almondtools/tante-emmas.git
  * as project from the provided USB drive
* The workshop script (lessons.pdf) is available
  * in the src/hints directory
  * or as additional file from the provided USB drive
* There are two different setups
  * install the project from maven `mvn clean install -Poffline`
  * or copy all dependencies from the provided USB drive 

---

# Get Familiar
## 10 min

* We launch the application, e.g. with a launch script
    * eclipse: TanteEmmas.launch
    * intellij: tante_emmas.xml -> .idea/runConfigurations
    * other: 
    ```Bash
    java -cp [classpath] net.amygdalum.tanteemmas.server.Server
    ```
* Browse to http://localhost:8080
* Navigate through the application
* Explore the source code and read the steps in your workshop script

---

# Install Testrecorder 
## 10 min

* Now we prepare the application for recording
* Put `AgentConfig.java`
  * from src/hints
  * into the package `net.amygdalum.tanteemmas.testrecorder`
  * in your src/main/java folder
  
----

* The Testrecorder instrumentation agent is provided as jar file 

    ```Bash
    testrecorder-0.3.12-jar-with-dependencies.jar
    ```

* Adjust the launch script by adding  
    ```Bash
    -javaagent:<path to instrumentation agent>=net.amygdalum.tanteemmas.testrecorder.AgentConfig
    ```
  * eclipse: run configurations -> TanteEmmas -> Arguments -> Vm Arguments
  * intellij: run -> edit configurations -> tante-emmas -> Vm Options
  * shell: 
    ```Bash
    java
      -cp [classpath]
      -javaagent:[...]
      net.amygdalum.tanteemmas.server.Server
    ```
* Follow the instructions in the workshop script

---

# Record Simple Methods 
## 15 min

* Let's record a simple method, it's easy: 
* Annotate `PriceCalculator.computeFairPrice` with `@Recorded`
* Start the launch script and browse to http://localhost:8080
* Follow the instructions in your workshop script

----

## Lessons learned

* Testrecorder records the complete state of 
  * the method's arguments
  * the method's result
  * the method's this object
  * the method's thrown exceptions 

----

## Special Features
  * exceptions are recorded in a special way
  * testrecorder uses existing constructors and setters
  * testrecorder does not rely on common conventions (set-methods that are no plain setters are not considered)

---

# Global Dependencies 
## 10 min

* Slightly harder - we certainly cannot record the complete JVM state
* We should not record every existing static variable, only the one that are related to our test case
* So let's see how testrecorder supports the recording of static state 
* Annotate `PriceCalculator.applyUnfairCharges` with `@Recorded`
* Start the launch script and browse to http://localhost:8080
* Follow the instructions in your workshop script 
* Possibly affected API: `AgentConfig.getGlobals`, `@Global`, `Fields`  

----

## Lessons learned

* Testrecorder records selected static variables
* Testrecorder ignores all other static variables

---

# Input Dependencies 
## 15 min

* Most state influencing method behavior is available as state of variables
* Yet there is state that is read while executing the method - we call this input dependencies 
* Annotate `PriceCalculator.computePrice` with `@Recorded`
* Start the launch script and browse to http://localhost:8080
* Follow the instructions in your workshop script 
* Possibly affected API: `AgentConfig.getInputs`, `@Input`, `Methods`  

----

## Lessons learned

* Testrecorder records selected input sources
* Input is not restricted to files, streams, ... every object could be an input
* Input originating from third party code can be selected via `AgentConfig`

---

# Output Dependencies 
## 15 min

* Most state characterizing method behavior is available as state of variables
* Yet there is state the is written while executing the method - we call this output dependencies 
* Annotate `PriceCalculator.order` with `@Recorded`
* Start the launch script and browse to http://localhost:8080
* Follow the instructions in your workshop script 
* Possibly affected API: `AgentConfig.getOutputs`, `@Output`, `Methods`  

----

## Lessons learned

* Testrecorder records and verifies selected output sources
* Output is not restricted to files, streams, ... every object could be an output
* Output originating from third party code can be selected via `AgentConfig`

---

# Fix Bug
## 15 min

* Much effort went into making Testrecorder generating maintainable and correct java code, why?
* This design decision helps us turning to good software engineering methods (i.e. TDD and Refactoring)
* So let's see how we could handle bugs in the code
* Follow the instructions in your workshop script 

----

## Lessons learned

* Once generated we could change and refactor tests
* Some other tests will probably break, yet they give you valuable insights about the recorded methods
* There are different strategies how to handle generated tests if a bug was fixed
  * fixing all tests that fail to the new behavior (with a sharp eye whether the changes in behavior are ok)
  * repeating the recording with new behavior
  * rerunning the generated tests with testrecorder (generating tests from tests, yet input/output is not captured in this case)

---

# Future Plans

* Generated Tests are highly redundant (many scenarios are covered multiple times)
  * there should be a method to define equivalence classes of tests
  * making it possible to keep only one of each equivalence class
* Generated Tests are too many
  * there should be a method to record only selected scenarios from the code

---

# Feel free to use or contribute
## http://testrecorder.amygdalum.net

  * a manual for configuration
  * a manual how to record methods and objects (with or without agent)
  * some examples how to extend testrecorder with new serializers/deserializers 

----

## State of Development

  * API will be quite volatile a long while
  * there are some hard objects that are not yet supported (especially native state or concurrency state)
  * there are probably some scenarios that do not work yet (please report them)

----

## Promise of Quality

  * there are many tests and many assertions (coverage > 90%)
  * each reported and verified issue will be assigned at least one reproducing test

----

## Contributions and feedback are welcome

  * https://github.com/almondtools/testrecorder

---

# Thank you!
