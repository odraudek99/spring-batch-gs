package mx.com.odraudek99.batch.jobs.fileProcessor.model;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ElementProcessor implements ItemProcessor<Element, AnotherElement> {

//    @Override
    public AnotherElement process(Element element) throws Exception {
        final String anotherElementId = element.getId() + "::" + element.getText();
        return new AnotherElement(anotherElementId);
    }
}