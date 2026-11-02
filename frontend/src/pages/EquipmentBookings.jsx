
import { useState } from 'react';
import { CalendarDays, Plus, Check, X } from 'lucide-react';
import '../styles/equipment.css';

const initialBookings = [
  {
    id: 1,
    equipment: 'Mahindra Tractor',
    farmer: 'Ravi Kumar',
    date: '2026-11-02',
    status: 'Pending'
  },
  {
    id: 2,
    equipment: 'Water Pump',
    farmer: 'Suresh',
    date: '2026-11-05',
    status: 'Approved'
  }
];

export default function EquipmentBookings() {
  const [bookings, setBookings] = useState(initialBookings);
  const [form, setForm] = useState({
    equipment: '',
    farmer: '',
    date: ''
  });

  const addBooking = (e) => {
    e.preventDefault();

    setBookings(prev => [
      ...prev,
      {
        ...form,
        id: Date.now(),
        status: 'Pending'
      }
    ]);

    setForm({ equipment: '', farmer: '', date: '' });
  };

  const changeStatus = (id, status) => {
    setBookings(prev =>
      prev.map(booking =>
        booking.id === id
          ? { ...booking, status }
          : booking
      )
    );
  };

  return (
    <div className="equipment-page">
      <div className="equipment-heading">
        <div>
          <h1>Equipment Bookings</h1>
          <p>Schedule machinery and manage booking approvals.</p>
        </div>
        <CalendarDays size={30} color="#268a52" />
      </div>

      <form className="equipment-booking-form" onSubmit={addBooking}>
        <h2>New Equipment Booking</h2>

        <div className="equipment-booking-fields">
          <select
            required
            value={form.equipment}
            onChange={e =>
              setForm({ ...form, equipment: e.target.value })
            }
          >
            <option value="">Select Equipment</option>
            <option>Mahindra Tractor</option>
            <option>Water Pump</option>
            <option>Combine Harvester</option>
          </select>

          <input
            required
            placeholder="Farmer Name"
            value={form.farmer}
            onChange={e =>
              setForm({ ...form, farmer: e.target.value })
            }
          />

          <input
            required
            type="date"
            value={form.date}
            onChange={e =>
              setForm({ ...form, date: e.target.value })
            }
          />

          <button className="equipment-add" type="submit">
            <Plus size={18} /> Book Equipment
          </button>
        </div>
      </form>

      <div className="equipment-table-wrap">
        <table className="equipment-table">
          <thead>
            <tr>
              <th>Equipment</th>
              <th>Farmer</th>
              <th>Booking Date</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>
            {bookings.map(booking => (
              <tr key={booking.id}>
                <td>{booking.equipment}</td>
                <td>{booking.farmer}</td>
                <td>{booking.date}</td>
                <td>{booking.status}</td>
                <td>
                  {booking.status === 'Pending' ? (
                    <div className="booking-actions">
                      <button
                        onClick={() =>
                          changeStatus(booking.id, 'Approved')
                        }
                      >
                        <Check size={16} /> Approve
                      </button>

                      <button
                        onClick={() =>
                          changeStatus(booking.id, 'Rejected')
                        }
                      >
                        <X size={16} /> Reject
                      </button>
                    </div>
                  ) : (
                    <span>Reviewed</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
