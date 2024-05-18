package guru.sfg.brewery.springframework.services.listeners;

import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResponse;
import guru.sfg.brewery.springframework.config.JmsConfig;
import guru.sfg.brewery.springframework.services.AllocateOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocateOrderListener {

    private final AllocateOrderService allocateOrderService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateOrderRequest request){


        BeerOrderDto beerOrder = request.getBeerOrder();

        Boolean allocationResult = false;
        Boolean allocationError = false;
        try{
            allocationResult = allocateOrderService.allocateOrder(beerOrder);
        }catch (Exception e){
            log.error(e.getMessage());
            allocationError = true;
        }

        AllocateOrderResponse response = AllocateOrderResponse.builder()
                .beerOrder(beerOrder)
                .pendingInventory(!allocationResult)
                .allocationError(allocationError)
                .build();

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE, response);
    }
}
