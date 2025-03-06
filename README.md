# Test connector with Midpoint


This is just a test connector for me while learning to create connectors with the ICF framework, especially a focus on Evolveum's midPoint

This has been tested on 4.8, 4.9, 4.9.1 and even 4.10-SNAPSHOT.

The code is basically a mess, but it works. If I ever find the time I will clean up the code and make it more readable and even up to my normal standards.

It uses Hibernate for the SQLite database stuff, and you'll probably notice I'm not very proficient with Hibernate
as this is my first time using it in over 12 years.

This connector will create a local sqlite database where it has all the users, the groups in their respective tables
and then there's also a 'UsersGroups' table where the users and groups are mapped.

Of course this connector has nothing to do in a production environment, it's just for testing purposes.