This is a straight forward Java implementation to process and analysis EDGAR data.

# Implementation and test

It was implemented with Java, It should run for any version of Java that is after Java 1.5. It included run.sh from root
and run_tests.sh from insight_testsuite directories. Both of them were tested under MAC OS.

# Unit tests

As an experienced programmer, I agree with that we should create unit tests to prevent regressions. However for this
project I don't really have class public member functions to test. My implementation only includes a main function with
two private static helpers. Technically we should have unit test for them. But here I don't want to be that
paranoid.

# Integration tests

I followed instructions and create a bunch of tests under test suites. However as mentioned in description we are not
allowed to upload a file over 100M to github.

# Optimization

I realized that we could potentially have more optimizations from two places. One place is to read the log file a chunk
instead of one line each time. We can read file into a buffered chunk and process it. Another place is to have separate
priority queue to remember logs we have so far when we scan logs line by line, then we don't need to detect expired
session each time we read one new log. We can peek priority queue to check if we need output a expired session. All of
those optimizations involve more complicated algorithms and trade-off considerations. I think that is beyond the scope
of this project. Would be happy to have further discussion on the matter.