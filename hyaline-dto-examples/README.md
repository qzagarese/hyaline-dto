Hyaline DTO Examples
====================

## hyalinedto-springboot-rest-example

A simple Spring Boot application running a REST service.
The service returns a `Person` object serialized in different ways, as shown in the [Getting started](https://github.com/Bluesoul/hyaline-dto/wiki/Getting-started) page.

Path | Result 
:-----|:-----------
`/` | default Jackson serialization
`/noaddress` | removes the `address` attribute
`/renamed` | renames fields `firstName` and `lastName` to `name` and `surname` respectively
`/inlined` | serializes field `address` as a `String` instead of an `Address` instance
`/withFullName` | adds field `fullName` as a concatenation of first and last. Shows how to use Hyaline objects as prototypes for new calls 
