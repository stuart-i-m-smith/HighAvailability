# HighAvailability

A lightweight mechanism allowing N processes to negotiate which is their primary instance to do work. 

The oldest living failover instance will be notified once a primary instance is no longer available.

Backed by a hazelcast cluster.
