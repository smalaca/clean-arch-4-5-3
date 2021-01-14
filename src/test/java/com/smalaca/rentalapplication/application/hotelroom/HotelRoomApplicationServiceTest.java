package com.smalaca.rentalapplication.application.hotelroom;

import com.google.common.collect.ImmutableMap;
import com.smalaca.rentalapplication.domain.booking.Booking;
import com.smalaca.rentalapplication.domain.booking.BookingAssertion;
import com.smalaca.rentalapplication.domain.booking.BookingRepository;
import com.smalaca.rentalapplication.domain.event.FakeEventIdFactory;
import com.smalaca.rentalapplication.domain.eventchannel.EventChannel;
import com.smalaca.rentalapplication.domain.hotel.HotelRoom;
import com.smalaca.rentalapplication.domain.hotel.HotelRoomAssertion;
import com.smalaca.rentalapplication.domain.hotel.HotelRoomBooked;
import com.smalaca.rentalapplication.domain.hotel.HotelRoomRepository;
import com.smalaca.rentalapplication.infrastructure.clock.FakeClock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.smalaca.rentalapplication.domain.hotel.HotelRoom.Builder.hotelRoom;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

class HotelRoomApplicationServiceTest {
    private static final String HOTEL_ID = UUID.randomUUID().toString();
    private static final int ROOM_NUMBER = 13;
    private static final Map<String, Double> SPACES_DEFINITION = ImmutableMap.of("RoomOne", 20.0, "RoomTwo", 20.0);
    private static final String DESCRIPTION = "What a lovely place";
    private static final String TENANT_ID = "4321";
    private static final List<LocalDate> DAYS = asList(LocalDate.now(), LocalDate.now().plusDays(1));
    private static final String HOTEL_ROOM_ID = "7821321";

    private final HotelRoomRepository hotelRoomRepository = Mockito.mock(HotelRoomRepository.class);
    private final BookingRepository bookingRepository = Mockito.mock(BookingRepository.class);
    private final EventChannel eventChannel = Mockito.mock(EventChannel.class);
    private final HotelRoomApplicationService service = new HotelRoomApplicationServiceFactory().hotelRoomApplicationService(
            hotelRoomRepository, bookingRepository, new FakeEventIdFactory(), new FakeClock(), eventChannel);

    @Test
    void shouldCreateHotelRoom() {
        ArgumentCaptor<HotelRoom> captor = ArgumentCaptor.forClass(HotelRoom.class);

        service.add(givenHotelRoomDto());

        then(hotelRoomRepository).should().save(captor.capture());
        HotelRoomAssertion.assertThat(captor.getValue())
                .hasHotelIdEqualTo(HOTEL_ID)
                .hasRoomNumberEqualTo(ROOM_NUMBER)
                .hasSpacesDefinitionEqualTo(SPACES_DEFINITION)
                .hasDescriptionEqualTo(DESCRIPTION);
    }

    @Test
    void shouldReturnIdOfNewHotelRoom() {
        given(hotelRoomRepository.save(any())).willReturn(HOTEL_ROOM_ID);

        String actual = service.add(givenHotelRoomDto());

        Assertions.assertThat(actual).isEqualTo(HOTEL_ROOM_ID);
    }

    private HotelRoomDto givenHotelRoomDto() {
        return new HotelRoomDto(HOTEL_ID, ROOM_NUMBER, SPACES_DEFINITION, DESCRIPTION);
    }

    @Test
    void shouldCreateBookingWhenHotelRoomBooked() {
        String hotelRoomId = "1234";
        givenHotelRoom(hotelRoomId);

        service.book(givenHotelRoomBookingDto(hotelRoomId));

        thenBookingShouldBeCreated();
    }

    @Test
    void shouldPublishHotelRoomBookedEvent() {
        ArgumentCaptor<HotelRoomBooked> captor = ArgumentCaptor.forClass(HotelRoomBooked.class);
        String hotelRoomId = "1234";
        givenHotelRoom(hotelRoomId);

        service.book(givenHotelRoomBookingDto(hotelRoomId));

        then(eventChannel).should().publish(captor.capture());
        HotelRoomBooked actual = captor.getValue();
        assertThat(actual.getEventId()).isEqualTo(FakeEventIdFactory.UUID);
        assertThat(actual.getEventCreationDateTime()).isEqualTo(FakeClock.NOW);
        assertThat(actual.getHotelId()).isEqualTo(HOTEL_ID);
        assertThat(actual.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(actual.getDays()).containsExactlyElementsOf(DAYS);
    }

    private HotelRoomBookingDto givenHotelRoomBookingDto(String hotelRoomId) {
        return new HotelRoomBookingDto(hotelRoomId, TENANT_ID, DAYS);
    }

    private void thenBookingShouldBeCreated() {
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        BDDMockito.then(bookingRepository).should().save(captor.capture());

        BookingAssertion.assertThat(captor.getValue())
                .isHotelRoom()
                .hasTenantIdEqualTo(TENANT_ID)
                .containsAllDays(DAYS);
    }

    private void givenHotelRoom(String hotelRoomId) {
        HotelRoom hotelRoom = createHotelRoom();
        BDDMockito.given(hotelRoomRepository.findById(hotelRoomId)).willReturn(hotelRoom);
    }

    private HotelRoom createHotelRoom() {
        return hotelRoom()
                .withHotelId(HOTEL_ID)
                .withNumber(ROOM_NUMBER)
                .withSpacesDefinition(SPACES_DEFINITION)
                .withDescription(DESCRIPTION)
                .build();
    }
}