#Count Me Up

Hershiv Haria <br/>
hershiv.haria@gmail.com

##Overview

This application is a test challenge submission. It provides a web service that tracks votes for a given set of candidates, and provides a front end to vote and display the current standings.

This README is a (neater) transcript of my (messy) design notes, along with instructions on running and testing the application.

##Requirement Analysis

###Basic Requiremnts
Full requirements were provided in a document, which I'm not uploading just in case it's going to be reused in the future (yes, I know this is a *public* GitHub repo, but hey). The basic requirements as I understand them are as follows:

1. Allow users to vote for any of 5 candidates.
2. Users may vote a maximum of 3 times.
3. The results of the vote should be displayed on a web page.

Additional further requirements recommend updating the results table every second, to enable real-time tracking of the results.

###Questions
Following reading through the requirements and noting out the above, the following questions came to mind. As I couldn't ask before development, here they are now:

1. The requirements document refers to percentages often. Given that people vote in singular increments, and that the results appear to be required as integers counts of votes, not percentages of the voting populace, why the focus on percentages?
2. Are the number of candidates fixed?

###Assumptions
Pondering these questions and the requirements lead towards application design, which lead to the following assumptions:

1. There is a fixed number of candidates (5). *(I do make this configurable on the web service, but didn't get around to make it a configuration option to also update the javascript.)*
2. Candidates will simply be referenced by a number e.g. candidate-1.
3. Users would be signed in to track their votes. *(I simply ask them to enter an email address when voting, to use it as an identifier - the more unscroupulous amongst you may cheat by entering any old email address to get more votes...)*
4. Once users have voted 3 times, it's prevent them from getting to the voting form. *(I ended up skipping past this anyway, as it appeared more efficient to let them attempt to vote and simply have the web service skip the voting if they'd hit their limit - I'd have to do the check everytime anyway.)*

With these requirements and assumptions in place, I could design my application.

##Application Design

###Components

My application design aimed to follow the simple MVC tennets, with an underlying data model in my database, the web service providing application data management, and the front end simply being a way to display and request interaction with the data. By having a separate database manager class and web service functional class, we decouple the underlying db from the web service.

This was an important decision up front, as I'd decided I was going to run with a SQLite database to get up and running fast. I was unsure whether it would give me the performance I would need to achieve the sub-second response times from the advanced requirements, and luckily it did. However, as of writing I find the lack of support for connection pools causes clashes on the filelock when I get to testing real-time updates (more on that later). By having a db manager class, it's possible to replace SQLite in the future for something like mySQL, which would help us avoid these issues. **__(See final update below)__**

To reduce complexity when trying to count up how many votes a user had cast, I decided to simply create 2 tables - one which tracked the votes per candidate, and the other to track the votes per user. This allowed me to do simple lookups instead of needing to run expensive aggregation queries.

Also, I've built a completely decoupled front-end - HTML, CSS and JS, with no requirements (other than CORS and the requirements doc) to be on the same server. This flexibility means that we can develop any front-end we want, and have it interact with our web-service.

###Testing

The application was developed in a TDD manner. Having decided the architecture I wanted to follow best practices and defined a few tests that would allow me to ensure I was developing to the requirements.

1. voteTest
 - Send a vote to the web service
 - Expected: success response

2. tooManyVotesTest
 - Send 4 votes
 - Expected: Success response for the first 3, graceful failure for the 4th

3. countMeUpTest
 - Set up a predefined list of votes, request the tally.
 - Expected: Totals for each candidate match our votes.
  
Additionally, I planned to test my applications ability to serve real-time results. To do this, I planned a method that would submit a large number of random votes. This function should be long running (several seconds), to allow the front end to update whilst it was running.

##Deploying and Testing

###Deployment

1. Run to build the project files using the provided build.xml file.
`ant build`
2. Start glassfish server
*`PATH/TO/GLASSFISH/`*`bin/asadmin start-domain`
3. Deploy the application from the project root
*`PATH/TO/GLASSFISH/`*`bin/asadmin deploy out/artifacts/HH_CountMeUp_war_exploded`

### Testing

Once deployed, the application can be used by loading up your localhost address in a browser and appending the path `HH_CountMeUp_war_exploded`.

You may vote by entering your email address on the right and selecting a candidate to vote for. Additionally, you can select **Simulate Votes** to see the table update in real-time. *(As noted above, I did not get around to building a solution to the file locking issue that would suitably keep the request below 1 second, my fix would have been to use a mySQL database with connection pools.)* 

**__FINAL UPDATE: After testing on Windows, it appears the filesystem handles SQLite access gracefully. I have achieved sub-second responses without seeing any internal server errors.__** 

To run the jUnit tests, once the project has been built, run the following command from the project root:
* Mac/Unix: `java -classpath lib/*:out/production/HH-CountMeUp/:out/test/HH-CountMeUp/ org.junit.runner.JUnitCore CountMeUpTest`
* Windows: `java -classpath lib/*;out/production/HH-CountMeUp/;out/test/HH-CountMeUp/ org.junit.runner.JUnitCore CountMeUpTest`