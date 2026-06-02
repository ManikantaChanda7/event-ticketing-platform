import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useParams } from "react-router-dom";
import BannerShow from "../components/bannerShow";
import { fetchFilteredEvents } from "../redux/slices/eventSlice";
import SearchPage from "./searchResult";

const CategoryEventsPage = () => {
  const dispatch = useDispatch();
  const { categoryName } = useParams(); // 👈 read category from URL

  const { filteredEvents } = useSelector((state) => state.event);

  const bannerEvents = filteredEvents?.featured?.data?.data || [];

  const buildListQuery = () => {
    const query = {
      isFeatured: true,
      location: true,
      page: 1,
      limit: 10,
      category: categoryName, // 👈 lock category from URL
    };
    return query;
  };

  // 🔹 1) Fetch banner events for this category (top carousel)
  useEffect(() => {
    if (!categoryName) return;
    const params = {
      category: categoryName,
      isFeatured: true,
      location: true,
      page: 1,
      limit: 10,
    };
    dispatch(fetchFilteredEvents({ params, key: "featured" })).unwrap();
  }, [dispatch, categoryName]);

  // 🔹 2) Fetch paginated events for this category
  // useEffect(() => {
  //   if (!categoryName) return;
  //   const params = buildListQuery();
  //   dispatch(fetchFilteredEvents({ params, key: "categoryEvents" })).unwrap();
  // }, [dispatch, categoryName]);

  return (
    <div className="min-h-screen bg-white">
      {/* Banner for this category */}
      {bannerEvents.length > 0 && <BannerShow events={bannerEvents} />}

      {/* Reuse the full search component */}
      <SearchPage defaultCategory={categoryName} />
    </div>
  );
};

export default CategoryEventsPage;
