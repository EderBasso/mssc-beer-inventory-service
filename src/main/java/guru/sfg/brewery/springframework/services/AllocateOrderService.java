package guru.sfg.brewery.springframework.services;

import guru.sfg.brewery.model.BeerOrderDto;

public interface AllocateOrderService {
    Boolean allocateOrder(BeerOrderDto beerOrderDto);
}
