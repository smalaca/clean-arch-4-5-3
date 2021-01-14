package com.smalaca.rentalapplication.infrastructure.rest.api.hotel;

import com.smalaca.rentalapplication.application.hotel.HotelApplicationService;
import com.smalaca.rentalapplication.application.hotel.HotelDto;
import com.smalaca.rentalapplication.query.hotel.HotelReadModel;
import com.smalaca.rentalapplication.query.hotel.QueryHotelRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/hotel")
public class HotelRestController {
    private final HotelApplicationService hotelApplicationService;
    private final QueryHotelRepository queryHotelRepository;

    public HotelRestController(HotelApplicationService hotelApplicationService, QueryHotelRepository queryHotelRepository) {
        this.hotelApplicationService = hotelApplicationService;
        this.queryHotelRepository = queryHotelRepository;
    }

    @PostMapping
    public ResponseEntity<Void> add(@RequestBody HotelDto hotelDto) {
        String id = hotelApplicationService.add(hotelDto);

        return ResponseEntity.created(URI.create("/hotel/" + id)).build();
    }

    @GetMapping
    public Iterable<HotelReadModel> findAll() {
        return queryHotelRepository.findAll();
    }
}
