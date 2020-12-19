# DeGiro report generator
Inoffical report generator for csv files exported from a DEGIRO (C) depot.

This tool generates a HTML document with an overview about all the transactions in your depot (including the current year, which foremost was the reason I built this tool)

I could only test this with my own csv file. So the chance that this will NOT work with your csv file on the spot are quite high.

You can of course open issues or provide me with the line of csv that is not parsed correctly. (You may alter the data of course)

# Usage

Either use the provided *.jar file (/target/DgReportGenerator-0.1b-shaded) and the simple GUI to create your report or execute this using the command line like so:

java DgReportGenerator "<path-to-your-csv/csvFile.csv>"

# Known issues
The exported csv does NOT provide any hint of whether the asset is a sock or not. Right now this is determined by analysing the name.
This of course is an incomplete list and also introduce subtle bugs. If a stock contains the same characters it will be in a wrong group.


# License
This tool is released under GPL license
