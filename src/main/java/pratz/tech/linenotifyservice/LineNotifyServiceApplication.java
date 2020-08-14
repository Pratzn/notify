package pratz.tech.linenotifyservice;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
@Slf4j
@RestController
@SpringBootApplication
public class LineNotifyServiceApplication {

    //https://notify-bot.line.me/oauth/authorize?response_type=code&client_id=LovbFdIcGqmnyWyCE6QUSj&redirect_uri=http://nudklin.ddns.net:8080/callback&scope=notify&state=fdgsdgsdfgs

    @Configuration
    public static class Config{

        @Bean
        public RestTemplate restTemplate(){
            return new RestTemplateBuilder().build();
        }

    }

    public static void main(String[] args) {
        SpringApplication.run(LineNotifyServiceApplication.class, args);
    }

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/callback",method = RequestMethod.GET)
    public ResponseEntity<?> callback(@RequestParam(value = "code",required = true) String code,@RequestParam(value = "state",required = true) String state){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("client_id", "LovbFdIcGqmnyWyCE6QUSj");
        body.add("client_secret", "G3Jw0kxgpKLyaEJboCJTwheyAe7UWNFlT5XBftvOKeI");
        body.add("redirect_uri", "https://notifyz.herokuapp.com/callback");
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        ResponseEntity<String> json = restTemplate.exchange(
                "https://notify-bot.line.me/oauth/token", HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);
        String access_token = JsonPath.read(json.getBody(), "$.access_token");
        log.info("token : "+access_token);
        System.out.println("token : "+access_token);

        notify(access_token);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/notify",method = {RequestMethod.POST,RequestMethod.GET})
    public ResponseEntity<?> notify(@RequestParam(value = "access_token",required = true) String access_token){

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("Authorization", "Bearer "+access_token);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("message", "Your api key is "+access_token+", you can notify by this way \ncurl --location --request POST 'https://notify-api.line.me/api/notify' \\\n" +
                "--header 'Content-Type: application/x-www-form-urlencoded' \n" +
                "--header 'Authorization: Bearer "+access_token+"' \n" +
                "--data-urlencode 'message=<your message>'");
        ResponseEntity<String> json = restTemplate.exchange(
                "https://notify-api.line.me/api/notify", HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);
        int status = JsonPath.read(json.getBody(), "$.status");
        log.info("status : "+status);
        System.out.println("status : "+status);
        return ResponseEntity.ok().build();
    }

}
