package io.wkrzywiec.hexagonal.library.borrowing;

import io.wkrzywiec.hexagonal.library.borrowing.model.ActiveUser;
import io.wkrzywiec.hexagonal.library.borrowing.model.AvailableBook;
import io.wkrzywiec.hexagonal.library.borrowing.model.BookIdentification;
import io.wkrzywiec.hexagonal.library.borrowing.model.MaxReservationInterval;
import io.wkrzywiec.hexagonal.library.borrowing.model.OverdueReservation;
import io.wkrzywiec.hexagonal.library.borrowing.model.ReservationDetails;
import io.wkrzywiec.hexagonal.library.borrowing.model.ReservationId;
import io.wkrzywiec.hexagonal.library.borrowing.model.ReservedBook;
import io.wkrzywiec.hexagonal.library.borrowing.ports.outgoing.BorrowingDatabase;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryBorrowingDatabase implements BorrowingDatabase {

    ConcurrentHashMap<Long, ActiveUser> activeUsers = new ConcurrentHashMap<>();
    ConcurrentHashMap<Long, AvailableBook> availableBooks = new ConcurrentHashMap<>();
    ConcurrentHashMap<Long, ReservedBook> reservedBooks = new ConcurrentHashMap<>();

    @Override
    public void setBookAvailable(Long bookId) {
        availableBooks.put(bookId, new AvailableBook(bookId));
        reservedBooks.remove(bookId);
    }

    @Override
    public Optional<AvailableBook> getAvailableBook(Long bookId) {
        if (availableBooks.containsKey(bookId)) {
            return Optional.of(availableBooks.get(bookId));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ActiveUser> getActiveUser(Long userId) {
        if (activeUsers.containsKey(userId)) {
            return Optional.of(activeUsers.get(userId));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public ReservationDetails save(ReservedBook reservedBook) {
        Long reservationId = new Random().nextLong();
        availableBooks.remove(reservedBook.getIdAsLong());
        reservedBooks.put(reservationId,  reservedBook);
        return new ReservationDetails(new ReservationId(reservationId), reservedBook);
    }

    @Override
    public List<OverdueReservation> findReservationsAfter(MaxReservationInterval maxReservationInterval) {
        return reservedBooks.values().stream()
                .filter(reservedBook ->
                        reservedBook.getReservedDateAsInstant()
                                .isAfter(Instant.now().plus(maxReservationInterval.getDays().getCount(), ChronoUnit.DAYS)))
                .map(reservedBook ->
                        new OverdueReservation(
                            new ReservationId(1L),
                            new BookIdentification(reservedBook.getIdAsLong())))
                .collect(Collectors.toList());
    }
}
