# priority-rate-limiter

A rate limiter which reserve for high priority request

## How it works

Given a rate limit, it will reserve a potion of it for high priority request. 
The rate limiter will periodly check the usage of the reserver amount to make proper
adjustment.
