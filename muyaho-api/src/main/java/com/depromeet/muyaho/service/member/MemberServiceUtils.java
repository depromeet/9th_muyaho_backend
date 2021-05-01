package com.depromeet.muyaho.service.member;

import com.depromeet.muyaho.exception.NotFoundException;
import com.depromeet.muyaho.domain.member.Member;
import com.depromeet.muyaho.domain.member.MemberRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MemberServiceUtils {

    static Member findMemberById(MemberRepository memberRepository, Long memberId) {
        Member findMember = memberRepository.findMemberById(memberId);
        if (findMember == null) {
            throw new NotFoundException(String.format("존재하지 않는 멤버 (%s) 입니다", memberId));
        }
        return findMember;
    }

}
