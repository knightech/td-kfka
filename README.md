# Title Discovery

## This service provides a stub for the title discovery service supplying mocked responses for the following genres:-

### Action 

`curl localhost:8080/title-discovery/items?genre=Action`

### Comedy 

`curl localhost:8080/title-discovery/items?genre=Comedy`

### BollyWood 

`curl localhost:8080/title-discovery/items?genre=BollyWood`

For any other genre it returns an empty list

If genre is missing from the request it returns a 400 Bad Request

