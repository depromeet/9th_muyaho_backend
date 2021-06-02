package com.depromeet.muyaho.batch.job;

import com.depromeet.muyaho.batch.config.UniqueRunIdIncrementer;
import com.depromeet.muyaho.domain.domain.dailystockamount.DailyStockAmount;
import com.depromeet.muyaho.domain.domain.dailystockamount.DailyStockAmountRepository;
import com.depromeet.muyaho.domain.domain.member.Member;
import com.depromeet.muyaho.domain.domain.member.MemberRepository;
import com.depromeet.muyaho.domain.service.memberstock.MemberStockRetrieveService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Configuration
public class CalculateDailyStockAmountConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final MemberStockRetrieveService memberStockRetrieveService;
    private final MemberRepository memberRepository;
    private final DailyStockAmountRepository dailyStockAmountRepository;

    @Bean
    public Job calculateDailyStockAmountJob() {
        return jobBuilderFactory.get("calculateDailyStockAmountJob")
            .incrementer(new UniqueRunIdIncrementer())
            .start(calculateDailyStockAmountStep())
            .build();
    }

    @Bean
    public Step calculateDailyStockAmountStep() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return stepBuilderFactory.get("calculateDailyStockAmountStep")
            .tasklet((contribution, chunkContext) -> {
                List<Member> members = memberRepository.findAll();
                List<DailyStockAmount> dailyStockAmounts = members.stream()
                    .map(Member::getId)
                    .map(memberId -> memberStockRetrieveService.getMemberInvestStatus(memberId).toDailyStockAmount(memberId, localDateTime))
                    .collect(Collectors.toList());
                dailyStockAmountRepository.saveAll(dailyStockAmounts);
                return RepeatStatus.FINISHED;
            })
            .build();
    }

}
