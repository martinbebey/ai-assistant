from duration import parse_duration
import pytest


def test_basic():
    assert parse_duration("1h") == 3600
    assert parse_duration("1h30m") == 5400
    assert parse_duration("90s") == 90


def test_empty():
    assert parse_duration("") == 0
    assert parse_duration("   ") == 0


def test_order_and_repeat():
    assert parse_duration("30m1h") == 5400
    with pytest.raises(ValueError):
        parse_duration("1h1h")


def test_invalid():
    with pytest.raises(ValueError, match="invalid duration"):
        parse_duration("90")
    with pytest.raises(ValueError, match="invalid duration"):
        parse_duration("h30m")


def test_fractional_hours():
    # product wants decimal hours
    assert parse_duration("1.5h") == 5400
