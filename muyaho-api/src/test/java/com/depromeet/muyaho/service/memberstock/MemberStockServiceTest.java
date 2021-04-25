package com.depromeet.muyaho.service.memberstock;

import com.depromeet.muyaho.domain.memberstock.MemberStock;
import com.depromeet.muyaho.domain.memberstock.MemberStockCreator;
import com.depromeet.muyaho.domain.memberstock.MemberStockRepository;
import com.depromeet.muyaho.domain.stock.Stock;
import com.depromeet.muyaho.domain.stock.StockCreator;
import com.depromeet.muyaho.domain.stock.StockMarketType;
import com.depromeet.muyaho.domain.stock.StockRepository;
import com.depromeet.muyaho.service.MemberSetupTest;
import com.depromeet.muyaho.service.memberstock.dto.request.AddMemberStockRequest;
import com.depromeet.muyaho.service.memberstock.dto.request.UpdateMemberStockRequest;
import com.depromeet.muyaho.service.memberstock.dto.response.MemberStockInfoResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class MemberStockServiceTest extends MemberSetupTest {

    @Autowired
    private MemberStockService memberStockService;

    @Autowired
    private MemberStockRepository memberStockRepository;

    @Autowired
    private StockRepository stockRepository;

    private Stock stock;

    @BeforeEach
    void setUpStock() {
        stock = stockRepository.save(StockCreator.createActive("code", "비트코인", StockMarketType.BITCOIN));
    }

    @AfterEach
    void cleanUP() {
        super.cleanup();
        memberStockRepository.deleteAllInBatch();
        stockRepository.deleteAllInBatch();
    }

    @Test
    void 멤버가_새롭게_소유한_주식을_등록한다() {
        // given
        int purchasePrice = 10000;
        int quantity = 5;

        AddMemberStockRequest request = AddMemberStockRequest.testInstance(stock.getId(), purchasePrice, quantity);

        // when
        memberStockService.addMemberStock(request, memberId);

        // then
        List<MemberStock> memberStockList = memberStockRepository.findAll();
        assertThat(memberStockList).hasSize(1);
        assertMemberStock(memberStockList.get(0), memberId, stock.getId(), purchasePrice, quantity);
    }

    @Test
    void 존재하지_않은_주식을_소유한_주식으로_등록하려하면_에러가_발생한다() {
        // given
        Long notExistStockId = 999L;

        AddMemberStockRequest request = AddMemberStockRequest.testInstance(notExistStockId, 10000, 10);

        // when & then
        assertThatThrownBy(() -> memberStockService.addMemberStock(request, memberId)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미_소유한_주식으로_등록한_주식을_다시_등록하려하면_에러가_발생한다() {
        // given
        memberStockRepository.save(MemberStockCreator.create(memberId, stock, 10000, 10));

        AddMemberStockRequest request = AddMemberStockRequest.testInstance(stock.getId(), 1000, 1);

        // when & then
        assertThatThrownBy(() -> memberStockService.addMemberStock(request, memberId)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 내가_소유한_주식_들을_조회하면_보유한_주식_정보와함께_조회된다() {
        // given
        String code = "KRW-BIT";
        String name = "비트코인";
        StockMarketType type = StockMarketType.BITCOIN;
        Stock stock = StockCreator.createActive(code, name, type);
        stockRepository.save(stock);

        int purchasePrice = 10000;
        int quantity = 10;
        MemberStock memberStock = MemberStockCreator.create(memberId, stock, purchasePrice, quantity);
        memberStockRepository.save(memberStock);

        // when
        List<MemberStockInfoResponse> responses = memberStockService.getMyStockInfos(memberId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getMemberStockId()).isEqualTo(memberStock.getId());
        assertThat(responses.get(0).getPurchasePrice()).isEqualTo(purchasePrice);
        assertThat(responses.get(0).getQuantity()).isEqualTo(quantity);
        assertThat(responses.get(0).getStock().getCode()).isEqualTo(code);
        assertThat(responses.get(0).getStock().getName()).isEqualTo(name);
        assertThat(responses.get(0).getStock().getType()).isEqualTo(type);
    }

    @Test
    void 다른_사람이_소유한_주식에_접근할_수없다() {
        // given
        memberStockRepository.save(MemberStockCreator.create(999L, stock, 10000, 10));

        // when
        List<MemberStockInfoResponse> responses = memberStockService.getMyStockInfos(memberId);

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    void 아무_주식도_소유하고_있지않을때_조회하면_빈_리스트가_반환된다() {
        // when
        List<MemberStockInfoResponse> responses = memberStockService.getMyStockInfos(memberId);

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    void 보유한_주식을_수정한다() {
        // given
        MemberStock memberStock = MemberStockCreator.create(memberId, stock, 10000, 10);
        memberStockRepository.save(memberStock);

        int purchasePrice = 30000;
        int quantity = 999999;

        UpdateMemberStockRequest request = UpdateMemberStockRequest.testInstance(memberStock.getId(), purchasePrice, quantity);

        // when
        memberStockService.updateMemberStock(request, memberId);

        // then
        List<MemberStock> memberStockList = memberStockRepository.findAll();
        assertThat(memberStockList).hasSize(1);
        assertMemberStock(memberStockList.get(0), memberId, stock.getId(), purchasePrice, quantity);
    }

    @Test
    void 다른_사람이_소유한_주식에_대해서_수정할_수_없다() {
        // given
        MemberStock memberStock = MemberStockCreator.create(memberId, stock, 10000, 10);
        memberStockRepository.save(memberStock);

        int purchasePrice = 30000;
        int quantity = 999999;

        UpdateMemberStockRequest request = UpdateMemberStockRequest.testInstance(memberStock.getId(), purchasePrice, quantity);

        // when
        assertThatThrownBy(() -> memberStockService.updateMemberStock(request, 999L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 보유하지_않은_주식에대해서_수정할_수없다() {
        // given
        UpdateMemberStockRequest request = UpdateMemberStockRequest.testInstance(999L, 10000, 10);

        // when
        assertThatThrownBy(() -> memberStockService.updateMemberStock(request, memberId)).isInstanceOf(IllegalArgumentException.class);
    }


    private void assertMemberStock(MemberStock memberStock, Long memberId, Long stockId, int purchasePrice, int quantity) {
        assertThat(memberStock.getMemberId()).isEqualTo(memberId);
        assertThat(memberStock.getStock().getId()).isEqualTo(stockId);
        assertThat(memberStock.getPurchasePrice()).isEqualTo(purchasePrice);
        assertThat(memberStock.getQuantity()).isEqualTo(quantity);
    }

}
