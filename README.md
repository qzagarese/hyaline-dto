Hyaline DTO - A Java library for dynamic creation of Data Transfer Objects 
=====================

## News

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
### 22/01/2015

Examples section added. 
Added a Maven project showing how to use HyalineDTO with Spring Boot for creating dynamic bootable REST services. 





Hyaline is a simple library for creating Data Transfer Objects (DTO) in a single instruction.
It is designed to allow dynamic configuration of domain entities, so that you don't have to create new classes,
manage the entity-to-dto mapping or design a DTO that fits most of your use cases.

Hyaline is serialization-framework-agnostic, so you can stick with your favorite tools like Jackson.
Moreover, if your domain class has been written by somebody else and it uses a specific framework, Hyaline allows you to replace the configuration on the fly and keep your favorite framework.

## OK: less words, more code!

Here are few scenarios where Hyaline can help you.
Examples use Jackson, but you can use whatever you want. 

Your domain classes look like this:

```java

public class Person {

	private String firstName;
	
	private String lastName;
	
	private Address address;
	
	//getters and setters	
}	

public class Address {

	private String street;
	
	private int number;
	
	private String zipcode;
	
	private String city;
	
	private String country;

	//getters and setters
	
	@Override
	public String toString() {
		return number + " " + street + ", " + city + " " + zipcode + ", " + country;
	}
}	

```	
Jackson default serialization produces something like this:

```javascript
{
	"firstName" : "John",
	"lastName" : "Lennon",
	"address" : {
		"street" : "Abbey Road",
		"number" : 123,
		"zipcode" : "NW8 9AX",
		"city" : "London",
		"country" : "UK"
	}
}
```

### 1. I don't need the address: remove it.

```java
Person person = methodReturningInitializedPerson();
Person dto = Hyaline.dtoFromScratch(person, new DTO() {

			@JsonIgnore
			private Address address;

		});

```
Hyaline detects that field `address` matches a field in class `Person`, so it injects the `@JsonIgnore` configuration on that field.
Note that your `dto` will be assignable to your entity type, so if your method returned a `Person`, 
it can still return such type.

Here is the output:

```javascript
{
	"firstName" : "John",
	"lastName" : "Lennon"
}
```

### 2. Why firstName and lastName? Rename them!

```java
Person person = methodReturningInitializedPerson();
Person dto = Hyaline.dtoFromScratch(person, new DTO() {

			@JsonIgnore
			private Address address;
			
			@JsonProperty("name")
			private String firstName;
			
			@JsonProperty("surname")
			private String lastName;

		});

```
Output:

```javascript
{
	"name" : "John",
	"surname" : "Lennon"
}
```

### 3. Ok I need the address, but just as a string.

```java
final Person person = methodReturningInitializedPerson();
Person dto = Hyaline.dtoFromScratch(person, new DTO() {

			@JsonIgnore
			private Address address;
			
			@JsonProperty("address")
			private String stringAddress = person.getAddress().toString();
			
			@JsonProperty("name")
			private String firstName;
			
			@JsonProperty("surname")
			private String lastName;
			

		});

```
This is what I get:

```javascript
{
	"name" : "John",
	"surname" : "Lennon",
	"address" : "123 Abbey Road, London NW8 9AX, UK"
}
```

### 4. Well, I need to give a specific order to json properties and Jackson allows me to do it only at class level.

This is less elegant because of some limitations of the author and the Java compiler (suggestions are very welcome!!!).
Here is how to do it:

```java
final Person person = methodReturningInitializedPerson();
Person dto = Hyaline.dtoFromScratch(person, new DTO() {

			private MyTemplate t = new MyTemplate();
			
			@JsonPropertyOrder(alphabetic = true)
			class MyTemplate {
				@JsonIgnore
				private Address address;

				@JsonProperty("address")
				private String fullAddress = person.getAddress().toString();

				@JsonProperty("name")
				private String firstName;

				@JsonProperty("surname")
				private String lastName;
			}

		});

```
Tah daaaaah:

```javascript
{
	"address" : "123 Abbey Road, London NW8 9AX, UK",
	"name" : "John",
	"surname" : "Lennon"
}
```

## How to use it

Hyaline DTO can be simply imported by adding the following Maven dependency:

```xml

<dependency>
  <groupId>org.hyalinedto</groupId>
  <artifactId>hyalinedto</artifactId>
  <version>0.8-beta-1</version>
</dependency>

```

## Hyaline DTO - what is it and what it is not.

To better understand why you should use Hyaline, it is worth to explain what really Hyaline is, and a good starting point is to say what it is NOT.

### It is not a serialization framework

Hyaline is not a tool like Jackson, Kryo, Google JSON or Protocol Buffers. If you are a looking for a way to transform your Java entities into a text or binary representation, this is the wrong place, at least for now.

### It is not a wrapper for serialization frameworks

So you might think Hyaline wraps serialization frameworks and eases your work by defaulting reasonable settings. This approach is often useful, but it is not what Hyaline does.

### OK, so what is it?!

Hyaline is a tool to make objects representation dynamic, in order to better cope with quickly changing and contrasting requirements.
The driving principle is that you should not write a new class if you only need to modify a small detail of your object representation.
Moreover, mapping the domain entity to the data transfer object is boring, error prone and can require a lot of boilerplate code.
Finally, you might need more than one representation of your domain entity.

Think about a `User` class containing a list of `Order` entities.
- You might decide to serialize the orders only when you need to populate a view showing all user's orders.
- You might have to interact with a third party system requiring that your entity fields have specific names.
- You might want to flatten your object graph by putting a string representation of complex attributes, because the remote endpoint (e.g. client) logic must be kept as simple as possible.

### From scratch or from class?

Hyaline provides `Configuration by Exception` for data transfer objects.
If you need a small detail to be added or modified, you just provide that detail and this will not overwrite the general configuration.
To this end, Hyaline provides two methods:
- dtoFromScratch: if your domain entity has been annotated with any Java annotation for serialization configuration, your dto will be configured as if those annotations were not present.
- dtoFromClass: if your domain entity has been annotated with any Java annotation for serialization configuration, your dto will inherit such configuration for every class element, except for those where your template provides a matching element (e.g. a field with the same name as one of the entity fields) annotated with a different instance of the same annotation type.

An example can better explain the differences between the two methods.
Consider the previously mentioned class `Person`. If field `firstName` is annotated as follows:

```java
private @JsonProperty("name") String firstName; 
```
then the json representation of a dto coming from the following invocation:

```java
Person dto = Hyaline.dtoFromScratch(person, new DTO() {});
```
would be:

```javascript
{
	"firstName" : "John",
	"lastName" : "Lennon",
	// address here
}
```
Annotation `@JsonProperty` is completely ignored.
If the dto is created by invoking:

```java
Person dto = Hyaline.dtoFromClass(person, new DTO() {});
```
the json representation would be:

```javascript
{
	"name" : "John",
	"lastName" : "Lennon",
	// address here
}
```
Here annotation `@JsonProperty` has been applied.
Finally, if the dto is created as follows:

```java
Person dto = Hyaline.dtoFromClass(person, new DTO() {
	private @JsonProperty("theName") String firstName; 
});
```
the json representation would be:

```javascript
{
	"theName" : "John",
	"lastName" : "Lennon",
	// address here
}
```
In this case, the `DTO` template provides a field named `firstName` that matches the one declared by class `Person`, so the instance of `@JsonProperty` overwrites the one specified in class `Person`.  
