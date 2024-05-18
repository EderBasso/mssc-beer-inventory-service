package guru.sfg.brewery.springframework.services;

import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.BeerOrderLineDto;
import guru.sfg.brewery.springframework.domain.BeerInventory;
import guru.sfg.brewery.springframework.repositories.BeerInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Service
public class AllocateOrderServiceImpl implements AllocateOrderService {

    private final BeerInventoryRepository beerInventoryRepository;

    @Override
    public Boolean allocateOrder(BeerOrderDto beerOrderDto) {

        AtomicInteger totalAllocated = new AtomicInteger(0);
        AtomicInteger totalOrdered = new AtomicInteger(0);

        beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
            Integer orderQuantity = beerOrderLineDto.getOrderQuantity() == null ? 0 : beerOrderLineDto.getOrderQuantity();
            Integer allocatedQuantity = beerOrderLineDto.getAllocatedQuantity() == null ? 0 : beerOrderLineDto.getAllocatedQuantity();
            if(orderQuantity > allocatedQuantity) {
                allocateOrderLine(beerOrderLineDto);
            }
            allocatedQuantity = beerOrderLineDto.getAllocatedQuantity() == null ? 0 : beerOrderLineDto.getAllocatedQuantity();
            totalOrdered.addAndGet(orderQuantity);
            totalAllocated.addAndGet(allocatedQuantity);
        });

        return totalOrdered.get() == totalAllocated.get();
    }

    private void allocateOrderLine(BeerOrderLineDto beerOrderLineDto) {
        List<BeerInventory> beerInventories = beerInventoryRepository.findAllByUpc(beerOrderLineDto.getUpc());

        beerInventories.forEach(beerInventory -> {
            int inventory = beerInventory.getQuantityOnHand() == null ? 0 : beerInventory.getQuantityOnHand();
            int orderQuantity = beerOrderLineDto.getOrderQuantity() == null ? 0 : beerOrderLineDto.getOrderQuantity();
            int allocatedQuantity = beerOrderLineDto.getAllocatedQuantity() == null ? 0 : beerOrderLineDto.getAllocatedQuantity();
            int quantityToAllocate = orderQuantity - allocatedQuantity;

            if(inventory >= quantityToAllocate) {
                inventory -= quantityToAllocate;
                beerOrderLineDto.setAllocatedQuantity(orderQuantity);
                beerInventory.setQuantityOnHand(inventory);

                beerInventoryRepository.save(beerInventory);
            } else if (inventory > 0) {
                beerOrderLineDto.setAllocatedQuantity(allocatedQuantity + inventory);
                beerInventory.setQuantityOnHand(0);

                beerInventoryRepository.delete(beerInventory);
            }
        });
    }
}
