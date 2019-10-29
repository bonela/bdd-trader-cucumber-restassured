Feature: Viewing positions
  In order to understand how my investments are doing
  As a trader
  I want to be able to see the profits and losses for my investments

  All traders start with $1000 in cash in their portfolio
  CASH amounts are recorded in cents, so 50000 represents $500

  Background:
    Given the following market prices:
      | securityCode | price |
      | SNAP         | 200   |
      | IBM          | 60    |

  Scenario: Making a profit on a single share
    Given Lola Rubio is a registered client
    When Lola has purchased 5 SNAP shares at $100 price
    Then her positions should be:
      | securityCode | amount | totalValueInDollars | profit |
      | CASH         | 50000  | 500.00              | 0.00   |
      | SNAP         | 5      | 1000.00             | 500.00 |