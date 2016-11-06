package mx.com.odraudek99.batch;


import org.springframework.batch.item.ItemProcessor;

import mx.com.odraudek99.batch.exception.BatchException;

public class PersonItemProcessor implements ItemProcessor<Person, Person> {



    public Person process(final Person person) throws BatchException {
        final String firstName = person.getFirstName().toUpperCase();
        final String lastName = person.getLastName().toUpperCase();

        final Person transformedPerson = new Person(firstName, lastName);
         
        System.out.println("Converting (" + person + ") into (" + transformedPerson + ")");

        return transformedPerson;
    }

}