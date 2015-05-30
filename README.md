## Breaking news!
Hyaline is becoming a library for prototype objects in Java.
Check out the [Spring Boot example](https://github.com/Bluesoul/hyaline-dto/blob/hyaline-prototypes/hyaline-dto-examples/hyalinedto-springboot-rest-example/src/main/java/org/hyalinedto/examples/springbootrest/resources/PrototypeResource.java) in the [hyaline-prototypes](https://github.com/Bluesoul/hyaline-dto/tree/hyaline-prototypes) development branch! 

Hyaline DTO - A Java library for Dynamically Typed Objects 
=====================

Hyaline is a simple library for creating ~~Data Transfer Objects (DTO)~~ Dynamically Typed Objects in a single instruction.
Let's say you have a REST service and you want to return a specific JSON object without having to create a new type for that.
With Hyaline (+Spring) you can just write something like this:

```java
@RequestMapping("/myJSON")
public Object myJSON() throws HyalineException {
	return Hyaline.dtoFromScratch(new DTO(){
		String what = "The JSON I want.";
		String how = "Without creating a new class!";
		String when = "Now!!!! -> " + new Date();
	});
}
```
and you get something like this: 

```javascript
{
  "what":"The JSON I want.",
  "how":"Without creating a new class!",
  "when":"Now!!!! -> Sun Apr 19 14:50:41 BST 2015"
}
```
Well, pretty useless, but it basically shows that you can prototype your objects by putting whatever you need inside and get exactly what you expect.

####Use it with any annotation-based framework
Hyaline handles annotations, which means you can use it with most of the frameworks you like.
If, for instance, I want to rename one of the fields above in a quite dumb way, I might not rename the variable, but I could use a Jackson annotation:  

```java
return Hyaline.dtoFromScratch(new DTO(){
	String what = "The JSON I want.";
	String how = "Without creating a new class!";
	@JsonProperty("deadline")
	String when = "Now!!!! -> " + new Date();
});
```

###One more thing 
Hyaline works pretty well when it comes to create DTOs, but it is basically a tool that accepts stuff with annotations and spits out new types, so you can imagine a lot of possible scenarios (what about dynamic pojo validation or dynamic persistence mappings?). Imagination is the limit :-) 

##Where to go from here?

The [Getting started](https://github.com/Bluesoul/hyaline-dto/wiki/Getting-started) page is probably the best place :-)
You can also give a look at the [Spring boot example](https://github.com/Bluesoul/hyaline-dto/tree/master/hyaline-dto-examples) to see Hyaline in action!

## How to use it

A simple Maven dependency:

```xml

<dependency>
  <groupId>org.hyalinedto</groupId>
  <artifactId>hyalinedto</artifactId>
  <version>0.9.1</version>
</dependency>

```
or if you prefer Gradle:

```compile 'org.hyalinedto:hyalinedto:0.9.1' ``` 

## News
### 25/05/2015
Release 0.9.1 is out. Hyaline objects can now be passed as prototypes for new calls.
Check out the [Spring Boot example](https://github.com/Bluesoul/hyaline-dto/blob/master/hyaline-dto-examples/hyalinedto-springboot-rest-example/src/main/java/org/hyalinedto/examples/springbootrest/resources/PersonResource.java)(`/withFullName` endpoint) to see how! 

### 19/04/2015
Release 0.9.0 is finally out! 

### 22/03/2015

A new snapshot providing dynamic access to dynamically created fields and new tests is now available.
You can now access dynamic fields as follows:

```java
Person person = methodReturningInitializedPerson();
Person dto = Hyaline.dtoFromScratch(person, new DTO() {
	
		private String myNewField = "Hello World!";

		});
		
HyalineDTO proxy = (HyalineDTO) dto;		

String hello = (String) proxy.getAttribute("myNewField");

```

Remember that this way to access fields should be used only for dynamic fields.
Inherited fields can be accessed in a more efficient way by using getters and setters.


### 13/03/2015

A new snapshot containing several bug fixes is now available.

```xml

<dependency>
  <groupId>org.hyalinedto</groupId>
  <artifactId>hyalinedto</artifactId>
  <version>0.9.0-SNAPSHOT</version>
</dependency>

<repository>
  <id>oss-sonatype</id>
  <name>oss-sonatype</name>
  <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
  <snapshots>
    <enabled>true</enabled>
  </snapshots>
</repository>

```


