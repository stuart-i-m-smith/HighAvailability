# HighAvailability

A lightweight mechanism allowing N processes to negotiate a primary instance to do work. 

If a primary instance is no longer available, the oldest living failover instance will be notified as the new primary instance.

Backed by a hazelcast cluster.
