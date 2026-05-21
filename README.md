
# Simple ItemShop 1.1

A lightweight item-for-item shop plugin for Paper 1.21.1 servers.

## How it works
Place a chest, put a sign on top with the following format:
[Sklep]
ITEM_ID AMOUNT
ITEM_ID AMOUNT
[Closed]

Example - selling 10 dirt for 1 stone:
[Sklep]
DIRT 10
STONE 1
[Closed]

**Currently, the plugin only supports [Sklep] as the shop sign tag. Support for [Shop] will be added in a future update**

## Permissions
- itemshop.create - create and destroy own shops
- itemshop.use - buy from shops
- itemshop.admin - manage any shop

## Commands
- /itemshop language pl|en - change language
- /itemshop reload - reload config

