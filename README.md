# hello-world

Movie screening seat reservation system

Functional requirements
1. Choosing seats
2. Real time updates of taken seats
3. See available seats 
4. Cancel reservation 
5. Concurrent booking: only one reservation succeeds for a given seat 

Assumptions:
1. A single movie screening - ignoree multi movie scheduling
2. 1M users 10k seats for screening 100k reservation at peak

Out of scope:
1. Session timeout period - lock out other customers

Domain Models

1. Cinema -  List<Seats>
2. Seats - seatid (mandatory)
3. User - userid, seatid, reservationid
4. Reservation - reservationid (optional), seatid, userid, timestamp

Services

ReservationRequestServices - allows users to make a reservation, allow users to choose their seat. show the user which seats are available.
ModifyReservationServices - allows users to cancel their reservation


