# hello-world

Requirements
1. User can add product to cart
2. Remove product from cart
3. View contents of cart
4. Cart can have multiple products and same can appear more than once
5. No limit on cart
6. Cart sizes are the same

Assumption
1. userid is sufficient
2. 1 store is fine
3. just cart functionality 
4. in memory cart

Domain
1. Cart - cartid, products -> HashMap<String productname, int count>, userid
<!-- 2. Product - productname -->
3. User - userid
4. In-Memory Repository Map<User user, Cart cart>

Services
1. ShoppingService - users can interact with their cart 




