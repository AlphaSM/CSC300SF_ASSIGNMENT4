
# MKHSIY057 CSC3002F OS2 2024 Barman Scheduling Assignment

To run:
traverse to this file on terminal using "cd" command 
then when in the folder "OS2_skeletonCode" 
enter these commands in the terminal
    make clean 
    make 
    
and then to run program specify number of patrons in the simulation and the algorithm to use 
1 = First Come First Serve 
2 = Shortest Job First 
3 = Round Robin 
    make run ARGS="<number of patrons> <algorithm to use>"

run this command  
    make run ARGS="100 0" 
simulates 100 patrons with First Come First Serve

run this command  
    make run ARGS="100 1" 
simulates 100 patrons with Shortest Job First

run this command  
    make run ARGS="100 2" 
simulates 100 patrons with Round Robin

