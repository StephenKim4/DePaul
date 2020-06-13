#Stephen Kim
#Program Input Assignment
#CSC 376 - Karen Heart
#Youtube link - https://youtu.be/UT8xL9zOp68

import sys
import getopt

#Main function to read and print command line arguments
def main(argv):
	
	print("Standard Input:")
	
	#Take input and print out
	for line in sys.stdin:
		sys.stdout.write("%s" %line)
	
	#Initialize options 
	option1 = ""
	option2 = ""
	option3 = ""

	#Parse through input and get options and arguments
	try:
		opts, args = getopt.getopt(argv, "o:t:h", ["option1=",
			"option2=", "option3="])
	except getopt.GetoptError:
		print("GetoptError")
				
	#Print command line arguments
	print("Command line arguments: ")
	
	#Loop over options
	for opt, arg in opts:
		if opt in ('-o', "-option1"):
			option1 = arg
			print("option 1: " + option1)
	for opt, arg in opts:
		if opt in ('-t', "-option2"):
			option2 = arg
			print("option 2: " + option2)
	for opt, arg in opts:
		if opt in ('-h', "-option3"):
			option3 = arg
			print("option 3 " + option3)

#Run main with command line arguments
if __name__ == "__main__":
	main(sys.argv[1:])
	

