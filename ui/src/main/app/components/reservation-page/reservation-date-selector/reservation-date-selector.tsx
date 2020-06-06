import React, {useState} from 'react';
import {ScreeningActionPublisher} from "../../../redux/actions/screening";
import {Screening} from "../../../models/screening";
import {ReservationDateSelectorDekstopComponent} from "./desktop/reservation-date-selector-desktop";
import {ScreeningsState} from "../../../redux/reducers/screening-reducer";
import {connect, useDispatch} from "react-redux";
import {ReservationDateSelectorMobileComponent} from "./mobile/reservation-date-selector-mobile";

import './reservation-date-selector.scss';
import {useFetching} from "../../../utils/custom-fetch-hook";
import moment from "moment";

interface OwnProps {
    className: string;
    movieId: number;
    screeningActionPublisher: ScreeningActionPublisher;
    dateFormat?: string;
}

interface ScreeningsByDay {
    [formattedDate: string]: Screening[];
}

export interface GroupedScreeningsWithDates {
    groupedScreenings: ScreeningsByDay;
    formattedDates: string[];
}

interface ReservationDateSelectorState {
    screenings: Screening[];
    currentScreening?: Screening;
    isFetching: boolean;
}

export type ReservationDateSelectorProps = OwnProps & ReservationDateSelectorState;

const ReservationDateSelectorComponent = (props: ReservationDateSelectorProps) => {
    const {movieId, screeningActionPublisher, isFetching, screenings, dateFormat} = props;
    useFetching(screeningActionPublisher.fetchScreenings(movieId), [movieId]);

    if (isFetching) {
        return (
            <div className='info--message'>
                Fetching screenings...
            </div>
        )
    }

    const currentDateTimeFormat: string = dateFormat || 'DD/MM/YYYY';

    const getSortedScreenings = (screeningsToSort: Screening[]) => {
        const sortingRule = (first: Screening, second: Screening) => {
            return new Date(first.startTime).getTime() - new Date(second.startTime).getTime();
        };
        const tempScreenings = Object.assign([], screeningsToSort);
        tempScreenings.sort(sortingRule);
        return tempScreenings;
    };

    const getScreeningsByDate = (screenings: Screening[], withoutPastScreenings?: boolean): GroupedScreeningsWithDates => {
        const dates: string[] = [];
        const screeningsByDay: ScreeningsByDay = getSortedScreenings(screenings).reduce((acc: ScreeningsByDay, screening: Screening) => {
            const date = moment(screening.startTime);
            const formattedDate = date.format(currentDateTimeFormat);
            if (withoutPastScreenings && date.isBefore(moment.now())) {
                return acc;
            }
            if (!dates.includes(formattedDate)) {
                dates.push(formattedDate);
            }
            if (acc[formattedDate]) {
                acc[formattedDate].push(screening)
            } else {
                acc[formattedDate] = [screening]
            }
            return acc;
        }, {});
        return {
            groupedScreenings: screeningsByDay,
            formattedDates: dates
        }
    };
    const screeningsWithDates = getScreeningsByDate(screenings, true);

    return (
        <div className={props.className}>
            <ReservationDateSelectorDekstopComponent {...props} screeningsWithDates={screeningsWithDates}/>
            <ReservationDateSelectorMobileComponent  {...props} screeningsWithDates={screeningsWithDates}/>
        </div>
    );
};

const mapStateToProps = (store: any, ownProps: OwnProps) => {
    const screeningState: ScreeningsState = store.movieScreenings;
    return {
        screenings: screeningState.screenings,
        currentScreening: screeningState.currentScreening,
        isFetching: screeningState.isFetching,
        ...ownProps
    }
};


export const ReservationDateSelector: React.FC<OwnProps> = connect(mapStateToProps)(ReservationDateSelectorComponent);