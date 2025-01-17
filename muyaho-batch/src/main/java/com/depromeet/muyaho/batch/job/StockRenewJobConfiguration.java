package com.depromeet.muyaho.batch.job;

import com.depromeet.muyaho.batch.config.JobLoggerListener;
import com.depromeet.muyaho.batch.config.UniqueRunIdIncrementer;
import com.depromeet.muyaho.domain.domain.stock.StockMarketType;
import com.depromeet.muyaho.domain.service.stock.StockRenewService;
import com.depromeet.muyaho.domain.service.stock.dto.request.StockInfoRequest;
import com.depromeet.muyaho.external.client.bitcoin.upbit.UpBitApiCaller;
import com.depromeet.muyaho.external.client.stock.StockApiCaller;
import com.depromeet.muyaho.external.client.stock.StockType;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Configuration
public class StockRenewJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final ApplicationEventPublisher eventPublisher;

    private final UpBitApiCaller upBitApiCaller;
    private final StockApiCaller stockApiCaller;
    private final StockRenewService stockRenewService;

    @Bean
    public Job stockRenewJob() {
        return jobBuilderFactory.get("stockRenewJob")
            .incrementer(new UniqueRunIdIncrementer())
            .listener(JobListenerFactoryBean.getListener(new JobLoggerListener(eventPublisher)))
            .start(renewBitCoinStep())
            .next(renewDomesticStockStep())
            .next(renewOverseasStockStep())
            .build();
    }

    @Bean
    public Step renewBitCoinStep() {
        return stepBuilderFactory.get("renewBitCoinStep")
            .tasklet((contribution, chunkContext) -> {
                List<StockInfoRequest> bitCoinStocks = upBitApiCaller.fetchListedBitcoins().stream()
                    .map(market -> StockInfoRequest.of(market.getMarket(), market.getKoreanName()))
                    .collect(Collectors.toList());
                stockRenewService.renewStock(StockMarketType.BITCOIN, bitCoinStocks);
                return RepeatStatus.FINISHED;
            })
            .build();
    }

    @Bean
    public Step renewDomesticStockStep() {
        return stepBuilderFactory.get("renewDomesticStockStep")
            .tasklet((contribution, chunkContext) -> {
                List<StockInfoRequest> domesticStocks = stockApiCaller.fetchListedStocksCodes(StockType.DOMESTIC_STOCK).stream()
                    .map(market -> StockInfoRequest.of(market.getCode(), market.getName()))
                    .collect(Collectors.toList());
                stockRenewService.renewStock(StockMarketType.DOMESTIC_STOCK, domesticStocks);
                return RepeatStatus.FINISHED;
            })
            .build();
    }

    @Bean
    public Step renewOverseasStockStep() {
        return stepBuilderFactory.get("renewOverSeaStockStep")
            .tasklet((contribution, chunkContext) -> {
                stockRenewService.renewStock(StockMarketType.OVERSEAS_STOCK, fetchListedOverseasStock());
                return RepeatStatus.FINISHED;
            })
            .build();
    }

    private List<StockInfoRequest> fetchListedOverseasStock() {
        // NASDAQ 증시
        List<StockInfoRequest> overSeasStocks = stockApiCaller.fetchListedStocksCodes(StockType.NASDAQ).stream()
            .map(market -> StockInfoRequest.of(market.getCode(), market.getName()))
            .collect(Collectors.toList());

        // NYSE 증시
        overSeasStocks.addAll(stockApiCaller.fetchListedStocksCodes(StockType.NYSE).stream()
            .map(market -> StockInfoRequest.of(market.getCode(), market.getName()))
            .collect(Collectors.toList()));

        return overSeasStocks.stream()
            .distinct()
            .collect(Collectors.toList());
    }

}
