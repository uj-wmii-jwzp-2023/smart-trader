# Smart Trader

Online broker - allows traders to buy and sell stocks. 

Users can view and interact with bucket of popular stocks. They can view prices in real time and put orders to buy and sell selected securities.

Users can create three types of orders: market order, limit order and time-limit order. These orders differ in certainty of execution. Market orders buy or sell securities immediately at the best possible market price, limit orders set a specific price for buying or selling and are executed only if the market reaches that price or better, time-limit orders are limit orders which are automatically cancelled after certain time.

It is possible for users to view currently owned portfolio of stocks. They can also see the actual positions for all available financial instruments in the order book. The order book lists all the orders placed by traders, including the order type (buy or sell), quantity and price.

Trading can only occur within a scheduled time period.

Administrator user has the ability to add new types of stocks to the trading platform.

# Endpoints
| path | Method | Parameters | Permissions | Description | 
| :--- | :---:  | :---:      | :---:       | :---:       |
| USERS: |
| /users | GET | - | admin | return all users |
| /users/{id} | GET | - | admin, logged user | return specific user |
| /users/ | POST | username, password, name, surename, email, phone | all | creates new user (account), all parameters required |
| /users/{id} | PUT | username, password, name, surename, email, phone  | admin, logged user | updates user details, all parameters optional |
| /users/{id} | DELETE | - | admin, logged user | removes user (account) |
| /users/{id}/deposit | POST | cashAmount | logged user | deposit money to account  |
| /users/{id}/withdraw | POST | cashAmount | logged user | withdraw money from account |
| ORDERS: |
| /users/{id}/orders | GET | - | admin, logged user | return all active orders of that user |
| /users/{id}/orders/{id} | GET | - | admin, logged user | return specific order of that user |
| /users/{id}/orders/market/ | POST | stockId, quantity, type(buy/sell) | logged user | creates market order, removed if not matched directly, all parameters required |
| /users/{id}/orders/limit/ | POST | stockId, price, quantity, type(buy/sell) | logged user | creates limit order with specified price, all parameters required |
| /users/{id}/orders/time-limit/ | POST | stockId, price, quantity, type(buy/sell), cancelationTime | logged user | creates limit order that will be cancelled after specified time, all parameters required |
| /users/{id}/orders/limit/{id} | PUT | stockId, price, quantity, type(buy/sell) | logged user | change price, quantity or type of the order, updated parameters are optional |
| /users/{id}/orders/time-limit/{id} | PUT | stockId, price, quantity, type(buy/sell), cancelationTime | logged user | change price, quantity, type or cancelationTime of the order, updated parameters are optional |
| /users/{id}/orders/{id} | DELETE | - | admin, logged user | cancel order |
| STOCKS: |
| /stocks/ | GET | - | all | return all available stocks |
| /stocks/{id} | GET | - | all | return specific stock |
| /stocks/ | POST | name, ticker | admin | add new stock to the market, all parameters required |
| /stocks/{id} | PUT | name, ticker | admin | update stock details, all parameters optional |
| /stocks/{id} | DELETE | - | admin | removes stock from the market |
| /stocks/{id}/order-book | GET | - | all | get all active orders for that stock, only information about price, quantity and type should be returned, orders with equal price are merged, orders are returned in increasing (price) order  |
| SECURITY: |
| /login/ | POST | username, password | all | user login |


# Database
To store data we are using MongoDB. There will be three separate collections for storing users, stocks and orders.

## Users
- userId
- name
- surename
- username
- password
- email
- phoneNumber
- cashBalance
- portfolio (list)
	- stockId
	- stockVolume

## Stocks
- stockId
- ticker
- fullName
- currentPrice

## Orders
We have different types of orders, but all of them can be stored in the same format.
- orderId
- userdId
- stockId
- price
- quantity
- type (buy/sell)
- cancelationTime (null if there is no calcelation time)


# Server Details
- Available stocks from WIG20
- Stock prices are updated every 5(?) seconds and pending orders are executed if price is crossed
- Stocks can be traded between 9AM and 5PM CET.
- Orders can be inserted at any point, if they are inserted outside of trading window they will be executed when market opens.

# Running application
Running application with Docker
```console
docker build -t smarttrader .
docker run -p 8080:8080 smarttrader
```

