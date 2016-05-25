# BVA-Tool
A tool for performing Boundary Value Analysis on a Java method's source code.

# Overview
This is a fairly rudimentary tool that analyzes a Java method and generates a table of values for each input parameter. The values generated are values that are required to be tested by Boundary Value Analysis. To run this tool, build the Maven project and then run the application with the following command-line arguments:
1. The file path of the .java file that is to be parsed.
2. The name of the method to be analyzed. 

# Limitations
This tool as of right now only supports Java primitive types int, double, and char. Additionally, it is only capable of parsing simple conditional statements such as the ones provided in the Example class provided in this repository.

# Libraries Used
This tool makes use of the JavaParser library (http://javaparser.org/) as a method for reading java code.
