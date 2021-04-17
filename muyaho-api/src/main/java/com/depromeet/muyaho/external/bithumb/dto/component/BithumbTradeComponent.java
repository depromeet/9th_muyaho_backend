package com.depromeet.muyaho.external.bithumb.dto.component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ToString
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bitcoin.bithumb.trade")
public class BithumbTradeComponent {

    private String url;

}
