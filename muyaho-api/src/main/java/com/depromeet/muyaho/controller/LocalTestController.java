package com.depromeet.muyaho.controller;

import com.depromeet.muyaho.config.session.MemberSession;
import com.depromeet.muyaho.config.session.SessionConstants;
import com.depromeet.muyaho.domain.member.Member;
import com.depromeet.muyaho.domain.member.MemberProvider;
import com.depromeet.muyaho.domain.member.MemberRepository;
import com.depromeet.muyaho.schedule.StockScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * @Deprecated 연동되기 전까지 테스트용도의 API
 */
@Deprecated
@Profile({"local", "dev"})
@RequiredArgsConstructor
@RestController
public class LocalTestController {

    private static final Member testMember = Member.newInstance("test-uid", null, "테스트 계정", null, MemberProvider.KAKAO);

    private final MemberRepository memberRepository;
    private final HttpSession httpSession;
    private final StockScheduler scheduler;

    @GetMapping("/test-session")
    public ApiResponse<String> testSession() {
        Member findMember = memberRepository.findMemberByUidAndProvider(testMember.getUid(), MemberProvider.KAKAO);
        if (findMember == null) {
            findMember = memberRepository.save(testMember);
        }
        httpSession.setAttribute(SessionConstants.AUTH_SESSION, MemberSession.of(findMember.getId()));
        return ApiResponse.success(httpSession.getId());
    }

    @GetMapping("/renew/stock")
    public String renewDomesticStocks() {
        scheduler.renewStocksCode();
        return "OK";
    }

}