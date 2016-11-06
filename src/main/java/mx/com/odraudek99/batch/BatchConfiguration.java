package mx.com.odraudek99.batch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import mx.com.odraudek99.batch.jobs.fileProcessor.model.AnotherElement;
import mx.com.odraudek99.batch.jobs.fileProcessor.model.Element;
import mx.com.odraudek99.batch.jobs.fileProcessor.model.ElementProcessor;



@Component
public class BatchConfiguration {

    
    // tag::readerwriterprocessor[]
    @Bean (name={"reader1"})
    public ItemReader<Person> reader() {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
        reader.setResource(new ClassPathResource("sample-data.csv"));
        
        reader.setLineMapper(new DefaultLineMapper<Person>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] { "firstName", "lastName" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }});
        }});
        return reader;
    }

    @Bean(name={"processor1"})
    public ItemProcessor<Person, Person> processor() {
        return new PersonItemProcessor();
    }


    @Bean(name={"writer1"})
    @Autowired
    public ItemWriter<Person> writer(DataSource dataSource) {
    	FlatFileItemWriter<Person> writer = new FlatFileItemWriter<Person>();
    	
        writer.setResource(new FileSystemResource("/tmp/file.csv"));
        writer.setShouldDeleteIfEmpty(true);
        
        DelimitedLineAggregator<Person> lineAggregator = new DelimitedLineAggregator<Person>();
    	lineAggregator.setDelimiter(",");
    	BeanWrapperFieldExtractor<Person> fe = new BeanWrapperFieldExtractor<Person>();
    	lineAggregator.setFieldExtractor(fe);
    	fe.setNames(new String[]{"lastName", "firstName"});
    	
    	writer.setLineAggregator(lineAggregator);
        
        return writer;
    }
    
    
    
    @Bean(name={"writer2"})
    @Autowired
    public JdbcBatchItemWriter<Person> writer2(DataSource dataSource) {
        JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
        writer.setSql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)");
        writer.setDataSource(dataSource);
        
        return writer;
    }
    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean(name={"importUserJob"})
    @Autowired
    public Job importUserJob(JobRepository jobRepository, 
    @Qualifier("step1") Step step1, @Qualifier("processFileStep") Step processFileStep,
    JobExecutionListener jobListener) {
        JobBuilderFactory jobs=new JobBuilderFactory( jobRepository);
        
        Flow flowStep1 = new FlowBuilder<Flow>("flowStep1")
                        .start(step1)
                        .build();
        Flow flowStep2 = new FlowBuilder<Flow>("flowStep2")
                        .start(processFileStep)
                        .build();
        
        
        return jobs.get("importUserJob").start(flowStep1)
        		.on("FAILED")
                .end()
                .on("COMPLETED")
                .to( flowStep2  )
                .end()
                .build();
                

    }

    @Bean (name={"step1"})
    @Autowired 
    public Step step1(JobRepository jobRepository,
     	@Qualifier("reader1") ItemReader<Person> reader1, 
     	@Qualifier("writer2") JdbcBatchItemWriter<Person> writer2,
		@Qualifier("writer1") ItemWriter<Person> writer1,
		@Qualifier("processor1") ItemProcessor<Person, Person> processor1,
    	PlatformTransactionManager transactionManager) {
        StepBuilderFactory stepBuilderFactory = new StepBuilderFactory(jobRepository, transactionManager);
        return stepBuilderFactory.get("step1")
                .<Person, Person> chunk(1)
                .reader(reader1)
                .processor(processor1)
                .writer(writer1).writer(writer2)
                .build();
    }
    

    // end::jobstep[]

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    @Bean
    public JdbcTemplate jdbcTemplateSpring(DataSource dataSourceSpring) {
        return new JdbcTemplate(dataSourceSpring);
    }
    
    
    @Bean
    public JobExecutionListener listenerJob(final DataSource dataSourceSpring, final DataSource dataSource){
        JobExecutionListener listener=new JobExecutionListener() {


            public void afterJob(JobExecution arg0) {
                System.out.println( "Acces DB");
                JdbcTemplate jdbcTemplateSpring = new JdbcTemplate(dataSourceSpring);
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                
                List<Person> results = jdbcTemplate.query("SELECT first_name, last_name FROM people", new RowMapper<Person>() {
           

                    public Person mapRow(ResultSet rs, int row) throws SQLException {
                        return new Person(rs.getString(1), rs.getString(2));
                    }
                });
                

                for (Person person : results) {
                    System.out.println("Found <" + person + "> in the database.");
                }
                System.out.println("Estatisticas  de JOB");
                List<Map<String,String>> results1 = jdbcTemplateSpring.query("SELECT * FROM BATCH_JOB_INSTANCE", new RowMapper<Map<String, String>>() {
               

                    public Map<String, String> mapRow(ResultSet rs, int row) throws SQLException {
                         HashMap<String, String> map = new HashMap<String, String>();
                        
                        System.out.println(rs.getMetaData());
                        map.put("JOB_INSTANCE_ID",rs.getString("JOB_INSTANCE_ID"));
                        map.put("VERSION",rs.getString("VERSION"));
                        map.put("JOB_NAME",rs.getString("JOB_NAME"));
                        map.put("JOB_KEY",rs.getString("JOB_KEY"));
                        return map;
                    }
                });
                for (Map<String, String> info : results1) {
                    System.out.println("DB."+info);
                }
                
            }


            public void beforeJob(JobExecution arg0) {
                // TODO Auto-generated method stub
                System.out.println( "Acces DB");
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                List<Person> results = jdbcTemplate.query("SELECT first_name, last_name FROM people", new RowMapper<Person>() {


                    public Person mapRow(ResultSet rs, int row) throws SQLException {
                        return new Person(rs.getString(1), rs.getString(2));
                    }
                });
                

                for (Person person : results) {
                    System.out.println("Found <" + person + "> in the database.");
                }
                
                
            }
        };
        
        return listener;
        
    }

}
