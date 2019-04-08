package uk.co.novinet.rest;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.novinet.auth.MyBbAuthority;
import uk.co.novinet.service.audit.Audit;

import java.util.HashMap;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@Controller
public class HomeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/")
    @Audit
    public ModelAndView get() {
        return new ModelAndView("home", new HashMap<String, Object>() {{
            put("mybbAuthorityJqGridOpts", buildMyBbAuthorityJqGridOpts());
            put("mybbAuthorities", new Gson().toJson(stream(MyBbAuthority.values()).map(MyBbAuthority::getFriendlyName).collect(toList())));
            put("mybbAdminAuthorities", new Gson().toJson(stream(MyBbAuthority.values()).filter(MyBbAuthority::canAuthenticate).map(MyBbAuthority::getFriendlyName).collect(toList())));
            put("mybbPreRegistrationAuthorities", new Gson().toJson(stream(MyBbAuthority.values()).filter(MyBbAuthority::isPreRegisteredGroup).map(MyBbAuthority::getFriendlyName).collect(toList())));
            put("mybbBlockedAuthorities", new Gson().toJson(stream(MyBbAuthority.values()).filter(MyBbAuthority::isBlocked).map(MyBbAuthority::getFriendlyName).collect(toList())));
            put("mybbSelectableAsPrimaryGroupAuthorities", new Gson().toJson(stream(MyBbAuthority.values()).filter(MyBbAuthority::isSelectableAsPrimaryGroup).map(MyBbAuthority::getFriendlyName).collect(toList())));
        }});
    }

    private String buildMyBbAuthorityJqGridOpts() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(":Any");

        for (MyBbAuthority myBbAuthority : MyBbAuthority.values()) {
            stringBuilder.append(";").append(myBbAuthority.getFriendlyName()).append(":").append(myBbAuthority.getFriendlyName());
        }

        return stringBuilder.toString();
    }
}