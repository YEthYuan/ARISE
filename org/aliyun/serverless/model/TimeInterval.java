package org.aliyun.serverless.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class TimeInterval {
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeInterval() {
    }

    public TimeInterval(LocalDateTime start) {
        this.start = start;
    }

    public TimeInterval(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public Duration getDuration() {
        return Duration.between(this.getStart(), this.getEnd());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimeInterval that = (TimeInterval) o;
        return Objects.equals(getStart(), that.getStart()) && Objects.equals(getEnd(), that.getEnd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TimeInterval{");
        sb.append("start=").append(start);
        sb.append(", end=").append(end);
        sb.append('}');
        return sb.toString();
    }
}
