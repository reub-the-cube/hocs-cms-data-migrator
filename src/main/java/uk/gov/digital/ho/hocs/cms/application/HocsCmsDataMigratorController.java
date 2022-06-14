package uk.gov.digital.ho.hocs.cms.application;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HocsCmsDataMigratorController {

    @RequestMapping("/hello")
    String helloWorld(){
        return "helloWorld";
    }
}
