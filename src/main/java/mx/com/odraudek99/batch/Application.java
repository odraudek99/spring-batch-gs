package mx.com.odraudek99.batch;

import javax.sql.DataSource;


import org.springframework.batch.core.launch.support.CommandLineJobRunner;
import org.springframework.stereotype.Repository;

import mx.com.odraudek99.batch.exception.BatchException;


@Repository
public class Application {


    
    public static void main(String[] args) {
        try {
            CommandLineJobRunner.main(new String[] { "baseContext.xml",
                "importUserJob" });
        } catch (Exception e) {
        	
            System.out.println("ERROR EN EJECUCION");
        }
    }

    public void run(DataSource dataSource) throws BatchException {

    }
}
