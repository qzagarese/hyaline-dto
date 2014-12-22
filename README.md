Hyaline DTO - A Java library for dynamic creation of Data Transfer Objects 
=====================

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
Person person = methodReturningInitializedPerson();
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
Person person = methodReturningInitializedPerson();
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

## Hyaline is still a baby...

...so be good and please consider giving a feedback to help her growing up.
She will be very soon available on Maven, so stay tuned :-)


