package com.depromeet.muyaho.api.controller.stockhistory;

import com.depromeet.muyaho.api.event.memberstock.MemberStockDeletedEvent;
import com.depromeet.muyaho.api.service.stockhistory.StockHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StockHistoryEventListener {

    private final StockHistoryService stockHistoryService;

    @EventListener
    public void deleteMemberStockHistory(MemberStockDeletedEvent event) {
        stockHistoryService.deleteMemberStockHistory(event.getMemberStockId(), event.getMemberId());
    }

}