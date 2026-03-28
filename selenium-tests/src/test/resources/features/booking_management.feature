@booking-management
Feature: Booking Management
  As a registered user
  I want to manage my event bookings
  So that I can view, inspect, and cancel them as needed

  Background:
    Given the user is logged in with valid credentials
    And all existing bookings are cleared
    And the user books the first available event

  @part1 @smoke @regression
  Scenario: Booking appears in the bookings list after confirmation
    When the user views their bookings
    Then at least one booking should be listed
    And the booking should show a confirmed status

  @part2 @regression
  Scenario: Event and customer information appear on the booking page
    When the user views their bookings
    And the user opens their booking
    Then the booking reference should be confirmed
    And the booked event details should be shown
    And the customer information should be shown

  @part3 @regression
  Scenario: Payment information appears on the booking page
    When the user views their bookings
    And the user opens their booking
    Then the payment summary should be shown
    And the total amount paid should be displayed
    And the option to check refund eligibility should be available

  @part4 @regression
  Scenario: Cancelling a booking removes it from the list
    When the user views their bookings
    And the user opens their booking
    And the user cancels the booking
    Then the user should be returned to their bookings list
    And a cancellation confirmation should be displayed
    And no remaining bookings should be shown
