# Smart Trader

Online broker - allows traders to buy and sell stocks. 

Users can view and interact with bucket of popular stocks. They can view prices in real time and put orders to buy and sell selected securities.

Users can create two types of orders: market order and limit order. These orders differ in certainty of execution. Market orders buy or sell securities immediately at the best possible market price, while limit orders set a specific price for buying or selling and are executed only if the market reaches that price or better.

It is possible for users to view currently owned portfolio of stocks. They can also see the actual positions for all available financial instrument in the order book. The order book lists all the orders placed by traders, including the order type (buy or sell), quantity and price.

When two orders have the same price, priority is given to the earlier order.

Trading can only occur within a scheduled time period.

Administrator user has the ability to add new types of stocks to the trading platform.

# Endpoints
| path | Method | Desc |
| :---         |     :---:      |          ---: |
| /users  | GET     | return all users |
| /users/  | POST     | creates user |
| /users/{id}  | PATCH     | update user |
| /users/{id}  | DELETE     | removes user |
| /stock | GET | return all available stocks |
| /stock/  | POST     | creates stock |
| /stock/{id}  | PATCH     | update stock |
| /stock/{id}  | DELETE     | removes stock |
| /stock/order/market/{?} | POST     | creates market_order |
| /stock/order/limit/{?}  | POST     | creates market_order |
